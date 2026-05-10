package org.hyf.movie.mapper;

import org.hyf.movie.dto.ReviewRequestDTO;
import org.hyf.movie.dto.ReviewResponseDTO;
import org.hyf.movie.model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "movie", ignore = true)
    Review toEntity(ReviewRequestDTO dto);

    ReviewResponseDTO toResponseDTO(Review review);
}
