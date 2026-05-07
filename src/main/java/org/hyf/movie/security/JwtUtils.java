package org.hyf.movie.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

/**
 * Utility component for creating and validating JWT tokens.
 *
 * The secret is read from application properties (never hardcoded).
 * The expiration is configurable in milliseconds.
 */
@Component
public class JwtUtils {

    private final Key key;
    private final long expiration;

    public JwtUtils(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration}") long expiration
    ) {
        // Decode the Base64-encoded secret into raw bytes and build an HMAC-SHA256 key
        // This will do length checks and throw an exception if the key is too weak
        // Why don't we use a simple string? Because the key must be of sufficient length for security, and using a Base64-encoded string allows us to easily generate and manage keys of the correct length without worrying about character encoding issues.
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expiration = expiration;
    }

    /**
     * Generates a signed JWT token.
     *
     * @param subject the "sub" claim — typically the user's email
     * @param claims  additional claims to embed (e.g. userId, role)
     * @return signed JWT string
     */
    public String generateToken(String subject, Map<String, Object> claims) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Returns true if the token has a valid signature and has not expired.
     */
    public boolean isValid(String token) {
        try {
            Date tokenExpiration = parse(token).getBody().getExpiration();
            return tokenExpiration.after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extracts the subject (email) from a valid token.
     * Throws an exception if the token is invalid (bad signature, malformed, expired, etc.).
     */
    public String getSubject(String token) {
        Claims body = parse(token).getBody();
        System.out.println(body.getSubject() );
        return body.getSubject();
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token); // This will throw an exception if the token is invalid (bad signature, malformed, expired, etc.)
    }
}
