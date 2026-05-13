package org.hyf.movie.dto;

import lombok.Data;

@Data
public class ReviewResponseDTO {

    private Long id;
    private Integer rating;
    private String comment;

}
