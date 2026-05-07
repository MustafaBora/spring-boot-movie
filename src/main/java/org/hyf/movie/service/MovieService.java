package org.hyf.movie.service;

import org.hyf.movie.dto.*;
import org.hyf.movie.exception.MovieNotFoundException;
import org.hyf.movie.exception.ReviewNotFoundException;
import org.hyf.movie.mapper.HYFMovieMapper;
import org.hyf.movie.mapper.MovieMapper;
import org.hyf.movie.mapper.ReviewMapper;
import org.hyf.movie.model.Movie;
import org.hyf.movie.model.Review;
import org.hyf.movie.repository.MovieRepository;
import org.hyf.movie.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class MovieService {

    private final MovieRepository movieRepository;
    private final ReviewRepository reviewRepository;
    private final MovieMapper movieMapper;
    private final ReviewMapper reviewMapper;
    private final HYFMovieMapper hyfMapper;

    public MovieService(MovieRepository movieRepository,
                        ReviewRepository reviewRepository,
                        MovieMapper movieMapper,
                        ReviewMapper reviewMapper,
                        HYFMovieMapper hyfMapper) {
        this.movieRepository = movieRepository;
        this.reviewRepository = reviewRepository;
        this.movieMapper = movieMapper;
        this.reviewMapper = reviewMapper;
        this.hyfMapper = hyfMapper;
    }

    public List<Movie> getAll() {
        return movieRepository.findAll();
    }

    public Movie save(Movie movie) {
        return movieRepository.save(movie);
    }

    public HYFResponseMovieDTO findById(Long id) {
        Optional<Movie> byId = movieRepository.findById(id);
        Movie m = byId.orElseThrow(() -> new MovieNotFoundException(id));
        return hyfMapper.toResponseDTO(m);
    }

    public List<HYFResponseMovieDTO> findByDirector(String director) {
        List<Movie> byDirector = movieRepository.findByDirector(director);
        List<HYFResponseMovieDTO> mapped = byDirector.stream().map(hyfMapper::toResponseDTO).toList();
        return mapped;
    }











    @Transactional(readOnly = true)
    public Page<MovieResponseDTO> getAllMovies(Pageable pageable) {
        return movieRepository.findAll(pageable).map(movieMapper::toResponseDTO);
    }

    public MovieResponseDTO createMovie(MovieRequestDTO dto) {
        Movie movie = movieMapper.toEntity(dto);
        //TODO check if the title is existent already, if so, throw MovieAlreadyExistsExeption
        MovieResponseDTO movieResponseDTO = movieMapper.toResponseDTO(movieRepository.save(movie));
        return movieResponseDTO;
    }

    @Transactional(readOnly = true)
    public MovieResponseDTO getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));
        return movieMapper.toResponseDTO(movie);
    }

    /*
    Do this later
    public MovieResponseDTO patchMovie(Long id, MoviePatchDTO dto) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));
        movieMapper.updateFromPatch(dto, movie);
        return movieMapper.toResponseDTO(movieRepository.save(movie));
    }
    */

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

    @Transactional(readOnly = true)
    public List<MovieResponseDTO> getTopRated() {
        return movieRepository.findTopRated(PageRequest.of(0, 5)).stream()
                .map(movieMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

}
