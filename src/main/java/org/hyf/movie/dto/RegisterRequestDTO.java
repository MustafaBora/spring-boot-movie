package org.hyf.movie.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "It must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    //on registry we may have so much more information
}
