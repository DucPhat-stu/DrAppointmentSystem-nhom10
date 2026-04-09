package com.healthcare.auth.application;

import java.time.OffsetDateTime;

public record IssuedTokenPair(
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        OffsetDateTime issuedAt,
        OffsetDateTime refreshTokenExpiresAt
) {
}

