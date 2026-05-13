package org.hyf.movie.dto;

import lombok.Data;

@Data
public class ReviewRequestDTO {

    private Integer rating;
    private String comment;

}
