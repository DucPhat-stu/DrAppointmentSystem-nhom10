package com.healthcare.auth.application;

public record LoginResult(
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        String role
) {
}

