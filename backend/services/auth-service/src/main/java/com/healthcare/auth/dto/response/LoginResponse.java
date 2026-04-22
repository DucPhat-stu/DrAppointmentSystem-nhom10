package com.healthcare.auth.dto.response;

import com.healthcare.shared.security.Permission;

import java.util.Set;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        String role,
        Set<Permission> permissions
) {
}
