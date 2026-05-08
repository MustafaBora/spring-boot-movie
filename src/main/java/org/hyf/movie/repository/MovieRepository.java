package org.hyf.movie.repository;

import java.util.List;

import org.hyf.movie.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByDirector(String director);

    boolean existsMovieByTitle(String title);
}
