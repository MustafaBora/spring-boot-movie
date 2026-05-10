package org.hyf.movie.mapper;

import org.hyf.movie.dto.MoviePatchDTO;
import org.hyf.movie.dto.MovieRequestDTO;
import org.hyf.movie.dto.MovieResponseDTO;
import org.hyf.movie.model.Movie;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class MovieMapper {

    private final ReviewMapper reviewMapper;

    public MovieMapper(ReviewMapper reviewMapper) {
        this.reviewMapper = reviewMapper;
    }

    public Movie toEntity(MovieRequestDTO dto) {
        Movie movie = new Movie();
        movie.setTitle(dto.getTitle());
        movie.setDirector(dto.getDirector());
        movie.setReleaseYear(dto.getReleaseYear());
        return movie;
    }

    public MovieResponseDTO toResponseDTO(Movie movie) {
        MovieResponseDTO dto = new MovieResponseDTO();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setDirector(movie.getDirector());
        dto.setReleaseYear(movie.getReleaseYear());
        dto.setReviews(movie.getReviews().stream()
                .map(reviewMapper::toResponseDTO)
                .collect(Collectors.toList()));
        return dto;
    }



    public MovieRequestDTO toRequestDTO(Movie movie) {
        MovieRequestDTO dto = new MovieRequestDTO();
        dto.setTitle(movie.getTitle());
        return dto;
    }

    /*
    //do this later
    public void updateFromPatch(MoviePatchDTO dto, Movie movie) {
        if (dto.getTitle() != null) movie.setTitle(dto.getTitle());
        if (dto.getDirector() != null) movie.setDirector(dto.getDirector());
        if (dto.getReleaseYear() != null) movie.setReleaseYear(dto.getReleaseYear());
    }

     */
}
