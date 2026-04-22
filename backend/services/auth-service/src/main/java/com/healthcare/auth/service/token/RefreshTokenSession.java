package com.healthcare.auth.service.token;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RefreshTokenSession(
        UUID tokenId,
        UUID userId,
        String token,
        OffsetDateTime expiresAt,
        boolean revoked
) {
}
