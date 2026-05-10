package org.hyf.movie.service;

import org.hyf.movie.dto.LoginRequestDTO;
import org.hyf.movie.dto.LoginResponseDTO;
import org.hyf.movie.dto.RegisterRequestDTO;
import org.hyf.movie.model.RefreshToken;
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
 * Login         — verifies the hashed password and issues a JWT access token
 *                 plus a long-lived refresh token (README38).
 * Logout        — deletes the refresh token so it can never be reused.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // depends on the abstraction (DIP — README36)
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    public UserService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtUtils jwtUtils,
                        RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
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
     * Authenticates a user and returns both tokens.
     *
     * Access token  — short-lived JWT the client sends on every API request.
     * Refresh token — long-lived opaque token stored in the DB, used only to
     *                 get a new access token when the current one expires.
     *
     * Throws IllegalArgumentException if credentials are invalid.
     */
    public LoginResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        // matches() hashes the incoming plain-text password and compares with stored hash
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Embed userId and role as extra claims so the token carries user context
        Map<String, Object> claims = Map.of(
                "userId", user.getId(),
                "role", user.getRole().name()
        );

        String accessToken = jwtUtils.generateToken(user.getEmail(), claims);

        // Create (or replace) the refresh token in the database
        RefreshToken refreshToken = refreshTokenService.create(user);

        return new LoginResponseDTO(accessToken, refreshToken.getToken());
    }

    /**
     * Issues a new access token using a valid refresh token.
     * The old refresh token is rotated (replaced with a new one) for security.
     *
     * Throws IllegalArgumentException if the refresh token is invalid or expired.
     */
    public LoginResponseDTO refresh(String refreshTokenValue) {
        // validate() throws if the token doesn't exist in DB or is expired
        RefreshToken refreshToken = refreshTokenService.validate(refreshTokenValue);

        User user = refreshToken.getUser();

        Map<String, Object> claims = Map.of(
                "userId", user.getId(),
                "role", user.getRole().name()
        );

        String newAccessToken = jwtUtils.generateToken(user.getEmail(), claims);

        // Rotate: delete old refresh token, issue a new one
        RefreshToken newRefreshToken = refreshTokenService.rotate(refreshToken);

        return new LoginResponseDTO(newAccessToken, newRefreshToken.getToken());
    }

    /**
     * Logs out the user by deleting their refresh token from the database.
     * Their current access token will keep working until it naturally expires
     * (up to 15 minutes) — that is an accepted trade-off with stateless JWTs.
     */
    public void logout(User user) {
        refreshTokenService.deleteByUser(user);
    }
}
