package com.healthcare.auth.dto.response;

import com.healthcare.shared.security.Permission;

import java.util.Set;
import java.util.UUID;

public record LoginResponse(
        UUID userId,
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        String role,
        Set<Permission> permissions
) {
}
