package org.hyf.movie.mapper;

import org.hyf.movie.dto.MoviePatchDTO;
import org.hyf.movie.dto.MovieRequestDTO;
import org.hyf.movie.dto.MovieResponseDTO;
import org.hyf.movie.model.Movie;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@Mapper(componentModel = "spring", uses = ReviewMapper.class)
public interface MovieMapper {

    @Mapping(target="id", ignore = true)
    @Mapping(target="internalNotes", ignore = true)
    @Mapping(target="createdAt", ignore = true)
    @Mapping(target="budgetUsd", ignore = true)
    @Mapping(target="reviews", ignore = true)
    public Movie toEntity(MovieRequestDTO dto);

    public MovieResponseDTO toResponseDTO(Movie movie);

    public MovieRequestDTO toRequestDTO(Movie movie);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)    //for ignoring optional fields  if(dto.getDirector() != null) movie.setDirector(dto.getDirector());
    @Mapping(target="id", ignore = true)
    @Mapping(target="budgetUsd", ignore = true)
    @Mapping(target="internalNotes", ignore = true)
    @Mapping(target="createdAt", ignore = true)
    @Mapping(target = "reviews", ignore = true) // ?
    void updateFromPatch(MoviePatchDTO dto, @MappingTarget Movie movie);

}
