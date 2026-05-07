package org.hyf.movie.controller;

import jakarta.validation.Valid;
import org.hyf.movie.dto.LoginRequestDTO;
import org.hyf.movie.dto.LoginResponseDTO;
import org.hyf.movie.dto.RegisterRequestDTO;
import org.hyf.movie.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public endpoints for registration and login (README38).
 *
 * POST /auth/register — creates a new user account
 * POST /auth/login    — validates credentials and returns a JWT token
 *
 * Both endpoints are explicitly permitted in SecurityConfig
 * so no token is required to reach them.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequestDTO dto) {
        userService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(userService.login(dto));
    }
}
