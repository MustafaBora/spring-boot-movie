package org.hyf.movie.controller;

import jakarta.validation.Valid;
import org.hyf.movie.dto.*;
import org.hyf.movie.mapper.MovieMapper;
import org.hyf.movie.model.Movie;
import org.hyf.movie.service.MovieService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
@Validated  // enables validation on @PathVariable and @RequestParam (README32)
public class MovieController {

    private final MovieService movieService;
    private final MovieMapper movieMapper;

    public MovieController(MovieService movieService,
                           MovieMapper mapper) {
        this.movieService = movieService;
        this.movieMapper = mapper;
    }

    @GetMapping("/{id}")
    public HYFResponseMovieDTO findById(@PathVariable Long id) {
        HYFResponseMovieDTO byId = movieService.findById(id);
        return byId;
    }

    @GetMapping("/director")
    public List<HYFResponseMovieDTO> findByDirector(@RequestParam String director) {
        return movieService.findByDirector(director);
    }












    // GET /movies — paginated list
    @GetMapping
    public Page<MovieResponseDTO> getAllMovies(@PageableDefault(size = 10) Pageable pageable) {
        return movieService.getAllMovies(pageable);
    }

    // POST /movies — create movie
    @PostMapping
    public ResponseEntity<MovieResponseDTO> createMovie(@Valid @RequestBody MovieRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.createMovie(dto));
    }

    // GET /movies/top-rated — top 5 by average rating (must be before /{id})
    @GetMapping("/top-rated")
    public List<MovieResponseDTO> getTopRated() {
        return movieService.getTopRated();
    }

    // GET /movies/{id} — movie with reviews
    //@GetMapping("/{id}")
    /*public MovieResponseDTO getMovie(@PathVariable Long id) {
        return movieService.getMovieById(id);
    }*/

    /*
    we will do this later
    // PATCH /movies/{id} — partial update
    @PatchMapping("/{id}")
    public MovieResponseDTO patchMovie(@PathVariable Long id, @RequestBody MoviePatchDTO dto) {
        return movieService.patchMovie(id, dto);
    }
     */

    // DELETE /movies/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    // GET /movies/{id}/reviews
    @GetMapping("/{id}/reviews")
    public List<ReviewResponseDTO> getReviews(@PathVariable Long id) {
        return movieService.getReviews(id);
    }

    // POST /movies/{id}/reviews
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
