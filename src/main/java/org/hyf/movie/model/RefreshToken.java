package org.hyf.movie.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Represents a refresh token stored in the database.
 *
 * WHY store refresh tokens in the database?
 * -----------------------------------------
 * Access tokens (JWT) are self-contained — the server can verify them without
 * looking anything up. That is fast, but it also means you CANNOT invalidate one
 * before it expires (e.g. on logout or password change).
 *
 * Refresh tokens solve this:
 *   - They are long-lived (days/weeks) but stored in the DB, so you CAN delete them.
 *   - The client uses a refresh token to get a new short-lived access token.
 *   - On logout, you delete the refresh token → the user can never get a new access token.
 *
 * Flow:
 *   Login  →  server returns  { accessToken (15 min),  refreshToken (7 days) }
 *   Access token expires  →  client sends refreshToken  →  server returns new accessToken
 *   Logout  →  server deletes refreshToken from DB
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The raw token string sent to the client.
     * Generated with UUID.randomUUID() — random, unguessable, not a JWT.
     * Stored as-is (no hashing needed here, but production systems sometimes hash it).
     */
    @Column(nullable = false, unique = true)
    private String token;

    /**
     * The user this token belongs to.
     * One user can have one refresh token at a time.
     * (In multi-device systems you would allow several.)
     */
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * When this refresh token stops being valid.
     * Stored as Instant (UTC timestamp) — easier to compare with Instant.now().
     */
    @Column(nullable = false)
    private Instant expiresAt;
}
