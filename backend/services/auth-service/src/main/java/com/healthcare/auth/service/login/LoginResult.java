package com.healthcare.auth.service.login;

import com.healthcare.shared.security.Permission;

import java.util.Set;
import java.util.UUID;

public record LoginResult(
        UUID userId,
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        String role,
        Set<Permission> permissions
) {
}
