package org.hyf.movie.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MovieRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Director is required")
    private String director;

    @NotNull(message = "Release year is required")
    @Min(value = 1888, message = "Release year must be 1888 or later")
    @Max(value = 2027, message = "Release year must be 2027 or earlier")
    private Integer releaseYear;

}
