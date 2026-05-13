package org.hyf.movie.service;

import lombok.RequiredArgsConstructor;
import org.hyf.movie.dto.LoginRequestDTO;
import org.hyf.movie.dto.LoginResponseDTO;
import org.hyf.movie.dto.RegisterRequestDTO;
import org.hyf.movie.model.User;
import org.hyf.movie.repository.UserRepository;
import org.hyf.movie.security.JwtUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public void register(RegisterRequestDTO dto) {  //TODO we can return a RegisterResponseDTO
        if(userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + dto.getEmail());   //TODO write a specific exception and handle it on GlobalExceptionHandler
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));    //it actually is hash

        User saved = userRepository.save(user);
    }

    public LoginResponseDTO login(LoginRequestDTO dto) {
        //find the user in the DB
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email!"));
        //check if the hashed password matches with the hashed password in the DB
        if( ! passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password!");
        }
        //TODO make both of them the same exception / result. (invalid email invalid password

        Map<String, Object> claims = Map.of(
            "userId", user.getId(),
            "role", user.getRole()
        );

        //call JwtUtils to generate a JWTToken
        String token = jwtUtils.generateToken(user.getEmail(), claims);

        return new LoginResponseDTO(token);
        //return the user the token
    }

}
