package org.hyf.movie.service;

import org.hyf.movie.dto.LoginRequestDTO;
import org.hyf.movie.dto.LoginResponseDTO;
import org.hyf.movie.dto.RegisterRequestDTO;
import org.hyf.movie.model.User;
import org.hyf.movie.repository.UserRepository;
import org.hyf.movie.security.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Handles user registration and login.
 *
 * Registration  — hashes the password with BCrypt before saving (README36).
 * Login         — verifies the hashed password and issues a JWT token (README38).
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // depends on the abstraction (DIP — README36)
    private final JwtUtils jwtUtils;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Registers a new user.
     * Throws IllegalArgumentException if the email is already taken.
     */
    public void register(RegisterRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + dto.getEmail());
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        // encode() is one-way — there is no decode() (README36)
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        userRepository.save(user);
    }

    /**
     * Authenticates a user and returns a JWT token.
     * Throws IllegalArgumentException if credentials are invalid.
     */
    public LoginResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Embed userId and role as extra claims so the token carries user context
        Map<String, Object> claims = Map.of(
                "userId", user.getId(),
                "role", user.getRole().name()
        );

        String token = jwtUtils.generateToken(user.getEmail(), claims);
        return new LoginResponseDTO(token);
    }
}
