package org.hyf.movie.service;

import org.hyf.movie.dto.*;
import org.hyf.movie.exception.MovieAlreadyExistsException;
import org.hyf.movie.exception.MovieNotFoundException;
import org.hyf.movie.exception.ReviewNotFoundException;
import org.hyf.movie.mapper.MovieMapper;
import org.hyf.movie.mapper.ReviewMapper;
import org.hyf.movie.model.Movie;
import org.hyf.movie.model.Review;
import org.hyf.movie.repository.MovieRepository;
import org.hyf.movie.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MovieService {

    private final MovieRepository movieRepository;
    private final ReviewRepository reviewRepository;
    private final MovieMapper movieMapper;
    private final ReviewMapper reviewMapper;

    public MovieService(MovieRepository movieRepository,
                        ReviewRepository reviewRepository,
                        MovieMapper movieMapper,
                        ReviewMapper reviewMapper) {
        this.movieRepository = movieRepository;
        this.reviewRepository = reviewRepository;
        this.movieMapper = movieMapper;
        this.reviewMapper = reviewMapper;
    }

    public List<Movie> getAll() {
        return movieRepository.findAll();
    }

    public Movie save(Movie movie) {
        return movieRepository.save(movie);
    }



    public MovieResponseDTO createMovie(MovieRequestDTO dto) {
        Movie movie = movieMapper.toEntity(dto);
        if(movieRepository.existsByTitle(movie.getTitle())) {
            throw new MovieAlreadyExistsException(movie.getTitle());
        }
        MovieResponseDTO movieResponseDTO = movieMapper.toResponseDTO(movieRepository.save(movie));
        return movieResponseDTO;
    }

    @Transactional(readOnly = true)
    public MovieResponseDTO getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));
        return movieMapper.toResponseDTO(movie);
    }

    public MovieResponseDTO patchMovie(Long id, MoviePatchDTO dto) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));
        movieMapper.updateFromPatch(dto, movie);
        return movieMapper.toResponseDTO(movieRepository.save(movie));
    }

    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new MovieNotFoundException(id);
        }
        movieRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviews(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(movieId));
        return movie.getReviews().stream()
                .map(reviewMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public ReviewResponseDTO addReview(Long movieId, ReviewRequestDTO dto) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(movieId));
        Review review = reviewMapper.toEntity(dto);
        review.setMovie(movie);
        return reviewMapper.toResponseDTO(reviewRepository.save(review));
    }

    public void deleteReview(Long movieId, Long reviewId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(movieId));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));
        movie.getReviews().remove(review);
    }

}
