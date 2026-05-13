package org.hyf.movie.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class LoginRequestDTO {

    @Email(message = "Email mut be a valid email")
    private String email;

    @NotBlank(message = "Password is required")
    @Length(min = 6)
//    @Pattern(regexp = "sadas")
    private String password;

}
