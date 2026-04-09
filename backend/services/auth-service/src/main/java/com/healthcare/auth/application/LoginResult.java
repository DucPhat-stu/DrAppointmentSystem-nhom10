package com.healthcare.auth.application;

import com.healthcare.shared.security.Permission;

import java.util.Set;

public record LoginResult(
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        String role,
        Set<Permission> permissions
) {
}
