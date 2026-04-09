package com.healthcare.auth.web;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        String role
) {
}

