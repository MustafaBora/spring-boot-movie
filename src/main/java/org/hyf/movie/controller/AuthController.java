package org.hyf.movie.controller;

import jakarta.validation.Valid;
import org.hyf.movie.dto.LoginRequestDTO;
import org.hyf.movie.dto.LoginResponseDTO;
import org.hyf.movie.dto.RefreshRequestDTO;
import org.hyf.movie.dto.RegisterRequestDTO;
import org.hyf.movie.model.User;
import org.hyf.movie.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Public endpoints for registration, login, token refresh, and logout.
 *
 * POST /auth/register — creates a new user account
 * POST /auth/login    — validates credentials, returns accessToken + refreshToken
 * POST /auth/refresh  — exchanges a valid refreshToken for a new accessToken
 * POST /auth/logout   — deletes the refreshToken (user must log in again to get new tokens)
 *
 * All endpoints under /api/v1/auth/** are explicitly permitted in SecurityConfig
 * so no token is required to reach them — except /logout which needs the user in context.
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

    /**
     * The client calls this when its access token has expired.
     * It sends the refresh token it stored at login time.
     * The server validates it, issues a new access token, and rotates the refresh token.
     *
     * No Authorization header needed — the refresh token itself proves identity.
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(@Valid @RequestBody RefreshRequestDTO dto) {
        return ResponseEntity.ok(userService.refresh(dto.getRefreshToken()));
    }

    /**
     * Logout — deletes the refresh token from the database.
     *
     * @AuthenticationPrincipal injects the User object that JwtAuthenticationFilter
     * placed into the SecurityContext. This means the client MUST send a valid
     * access token in the Authorization header to reach this endpoint.
     *
     * After logout the access token technically still works until it expires (15 min),
     * but the refresh token is gone so the user cannot extend their session.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal User user) {
        userService.logout(user);
        return ResponseEntity.noContent().build(); // 204 No Content — success, nothing to return
    }
}
