package com.budgetix.auth.dto;

public record TokenResponse(
    String accessToken,
    String refreshToken,
    long expiresIn,
    String tokenType,
    UserAuthResponse user
) {
    public static TokenResponse of(String accessToken, String refreshToken, long expiresIn, UserAuthResponse user) {
        return new TokenResponse(accessToken, refreshToken, expiresIn, "Bearer", user);
    }
}
