package org.hyf.movie.repository;

import org.hyf.movie.model.RefreshToken;
import org.hyf.movie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // Look up a token by its string value — used when the client sends the token back
    Optional<RefreshToken> findByToken(String token);

    // Delete the token when a user logs out or when we want to force re-login
    // Spring Data JPA generates the DELETE query automatically from the method name
    void deleteByUser(User user);
}
