package org.hyf.movie.repository;

import org.hyf.movie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    public boolean existsByEmail(String email);

    public Optional<User> findByEmail(String email);

}
