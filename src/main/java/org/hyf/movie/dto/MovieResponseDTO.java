package org.hyf.movie.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class MovieResponseDTO {

    private Long id;
    private String title;
    private String director;
    private Integer releaseYear;
    private List<ReviewResponseDTO> reviews;

}
