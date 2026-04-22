package com.healthcare.auth.service.login;

import com.healthcare.shared.security.Permission;

import java.time.OffsetDateTime;
import java.util.Set;

public record IssuedTokenPair(
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        OffsetDateTime issuedAt,
        OffsetDateTime refreshTokenExpiresAt,
        Set<Permission> permissions
) {
}
