package com.healthcare.auth.application;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RefreshTokenRecord(
        UUID userId,
        String token,
        OffsetDateTime expiresAt,
        OffsetDateTime createdAt
) {
}

