package org.hyf.movie.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hyf.movie.dto.LoginRequestDTO;
import org.hyf.movie.dto.LoginResponseDTO;
import org.hyf.movie.dto.RegisterRequestDTO;
import org.hyf.movie.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequestDTO dto) {
        userService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody  LoginRequestDTO dto) {
        return ResponseEntity.ok(userService.login(dto) );
    }


}
