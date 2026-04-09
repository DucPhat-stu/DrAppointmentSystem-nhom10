package com.healthcare.auth.web;

import com.healthcare.shared.security.Permission;

import java.util.Set;

public record RefreshResponse(
        String accessToken,
        long expiresInSeconds,
        String role,
        Set<Permission> permissions
) {
}

