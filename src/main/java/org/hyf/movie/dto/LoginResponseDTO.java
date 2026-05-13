package org.hyf.movie.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {

    private String token;   //access token
//    private String refreshToken;    //refresh token may be implement
}
