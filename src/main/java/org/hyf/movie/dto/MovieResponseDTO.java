package org.hyf.movie.dto;

import java.util.List;

public class MovieResponseDTO {

    private Long id;
    private String title;
    private String director;
    private Integer releaseYear;
    private List<ReviewResponseDTO> reviews;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }

    public List<ReviewResponseDTO> getReviews() { return reviews; }
    public void setReviews(List<ReviewResponseDTO> reviews) { this.reviews = reviews; }
}
