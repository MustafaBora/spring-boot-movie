package org.hyf.movie.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for POST /auth/refresh.
 *
 * The client sends back the refresh token it received at login.
 * The server looks it up in the database, validates it, and returns a new access token.
 */
public class RefreshRequestDTO {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
