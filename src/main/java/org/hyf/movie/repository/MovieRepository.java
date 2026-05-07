package org.hyf.movie.repository;

import java.util.List;

import org.hyf.movie.model.Movie;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    @Query("SELECT m FROM Movie m LEFT JOIN m.reviews r GROUP BY m ORDER BY AVG(r.rating) DESC NULLS LAST")
    List<Movie> findTopRated(Pageable pageable);

    List<Movie> findByDirector(String director);
}
