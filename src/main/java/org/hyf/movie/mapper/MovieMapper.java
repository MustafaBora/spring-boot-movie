package org.hyf.movie.mapper;

import org.hyf.movie.dto.MoviePatchDTO;
import org.hyf.movie.dto.MovieRequestDTO;
import org.hyf.movie.dto.MovieResponseDTO;
import org.hyf.movie.model.Movie;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = ReviewMapper.class)
public interface MovieMapper {

    // id, internalNotes, createdAt, budgetUsd, reviews are ignored during creation
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "internalNotes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "budgetUsd", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    Movie toEntity(MovieRequestDTO dto);

    // reviews (List<Review> -> List<ReviewResponseDTO>) is handled automatically via uses = ReviewMapper.class
    MovieResponseDTO toResponseDTO(Movie movie);

    MovieRequestDTO toRequestDTO(Movie movie);

    // During patch update, we want to ignore null values in the DTO and also ignore certain fields that should not be updated
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "internalNotes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "budgetUsd", ignore = true)
    // We also ignore reviews here because we don't want to update the reviews list when patching a movie. Reviews should be managed separately via the ReviewController.
    @Mapping(target = "reviews", ignore = true)
    void updateFromPatch(MoviePatchDTO dto, @MappingTarget Movie movie);
}
