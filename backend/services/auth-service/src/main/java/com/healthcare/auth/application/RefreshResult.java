package com.healthcare.auth.application;

import com.healthcare.shared.security.Permission;

import java.util.Set;

public record RefreshResult(
        String accessToken,
        long expiresInSeconds,
        String role,
        Set<Permission> permissions
) {
}

