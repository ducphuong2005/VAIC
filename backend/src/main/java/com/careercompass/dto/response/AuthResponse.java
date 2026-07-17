package com.careercompass.dto.response;

public record AuthResponse(
        String tokenType,
        String accessToken,
        long expiresInSeconds,
        String refreshToken,
        UserResponse user
) {
}
