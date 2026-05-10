package org.hyf.movie.dto;

/**
 * Returned after a successful login.
 *
 * accessToken  — short-lived JWT (e.g. 15 minutes). Sent in the Authorization header on every request.
 * refreshToken — long-lived opaque token (e.g. 7 days). Stored by the client and used ONLY to get
 *                a new access token when the current one expires. Never sent to other endpoints.
 */
public class LoginResponseDTO {

    private String accessToken;
    private String refreshToken;

    public LoginResponseDTO(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
