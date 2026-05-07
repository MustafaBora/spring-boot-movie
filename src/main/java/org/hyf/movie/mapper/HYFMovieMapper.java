package org.hyf.movie.mapper;

import org.hyf.movie.dto.HYFResponseMovieDTO;
import org.hyf.movie.model.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface HYFMovieMapper {

    public HYFResponseMovieDTO toResponseDTO(Movie movie);


    /*public static HYFResponseMovieDTO toDTO(Movie m) {
        HYFResponseMovieDTO dto = new HYFResponseMovieDTO();
        dto.setTitle(m.getTitle());
        return dto;
    }*/
}
