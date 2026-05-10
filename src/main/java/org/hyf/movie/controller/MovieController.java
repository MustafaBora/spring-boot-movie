package org.hyf.movie.controller;

import jakarta.validation.Valid;
import org.hyf.movie.dto.*;
import org.hyf.movie.mapper.MovieMapper;
import org.hyf.movie.model.Movie;
import org.hyf.movie.service.MovieService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movies")
@Validated  // enables validation on @PathVariable and @RequestParam (README32)
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public List<Movie> getAllMovies() {
        return movieService.getAll();
    }

    // POST /movies — create movie
    @PostMapping
    public ResponseEntity<MovieResponseDTO> createMovie(@Valid @RequestBody MovieRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.createMovie(dto));
    }

    @GetMapping("/{id}")
    public MovieResponseDTO getMovie(@PathVariable Long id) {
        return movieService.getMovieById(id);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/reviews")
    public List<ReviewResponseDTO> getReviews(@PathVariable Long id) {
        return movieService.getReviews(id);
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<ReviewResponseDTO> addReview(@PathVariable Long id,
                                                       @RequestBody ReviewRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.addReview(id, dto));
    }

    // DELETE /movies/{id}/reviews/{reviewId}
    @DeleteMapping("/{id}/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id, @PathVariable Long reviewId) {
        movieService.deleteReview(id, reviewId);
        return ResponseEntity.noContent().build();
    }
}
