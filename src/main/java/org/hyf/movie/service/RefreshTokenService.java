package org.hyf.movie.service;

import org.hyf.movie.model.RefreshToken;
import org.hyf.movie.model.User;
import org.hyf.movie.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Manages the lifecycle of refresh tokens.
 *
 * Responsibilities:
 *   - create()   — generate and persist a new refresh token for a user
 *   - validate() — check that the token exists in the DB and has not expired
 *   - rotate()   — replace the old token with a brand-new one (best practice)
 *   - delete()   — remove the token on logout
 *
 * WHY a separate service?
 * Single Responsibility Principle — UserService handles authentication logic,
 * RefreshTokenService handles token storage. Easier to test and change independently.
 */
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * How long (in milliseconds) a refresh token stays valid.
     * Configured in application properties — default 7 days.
     * Much longer than the access token (15 minutes) because the user
     * should stay "logged in" across sessions without re-entering credentials.
     */
    private final long refreshExpiration;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${app.jwt.refresh-expiration}") long refreshExpiration
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshExpiration = refreshExpiration;
    }

    /**
     * Creates a new refresh token for the given user and saves it to the database.
     *
     * If the user already has a refresh token (e.g. they logged in on another device
     * or are logging in again), the old one is deleted first — one active token per user.
     *
     * UUID.randomUUID() produces a cryptographically random 128-bit value.
     * It is NOT a JWT — it has no payload, no signature. It is just a random key
     * that we look up in our database to find the associated user.
     */
    @Transactional
    public RefreshToken create(User user) {
        // Remove any existing token for this user before creating a new one
        // This prevents accumulation of stale tokens in the database
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString()); // random, unguessable string
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshExpiration));

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Looks up the token in the database and checks it has not expired.
     *
     * Throws IllegalArgumentException if:
     *   - the token string is not found (never existed, or was deleted on logout)
     *   - the token exists but the expiry time has passed
     *
     * This is the key advantage over a pure JWT refresh token:
     * we can REVOKE it by simply deleting the row.
     */
    public RefreshToken validate(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found — please log in again"));

        // Instant.now() is UTC current time. expiresAt is also UTC. Simple comparison.
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            // Expired tokens are cleaned up immediately to keep the table tidy
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("Refresh token has expired — please log in again");
        }

        return refreshToken;
    }

    /**
     * Token rotation — after a successful refresh, invalidate the old token and issue a new one.
     *
     * WHY rotate?
     * If an attacker steals the refresh token and uses it, rotation means:
     *   - The attacker gets a new token (bad)
     *   - But the legitimate user's old token is now invalid → next request fails → detects theft
     * Without rotation, the attacker could reuse the same token indefinitely.
     */
    @Transactional
    public RefreshToken rotate(RefreshToken oldToken) {
        return create(oldToken.getUser()); // create() already deletes the old one
    }

    /**
     * Called on logout — deletes the refresh token so it can never be reused.
     * After this, even a valid-looking token string will fail validate().
     */
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
