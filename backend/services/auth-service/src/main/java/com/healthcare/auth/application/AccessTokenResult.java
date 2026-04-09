package com.healthcare.auth.application;

import com.healthcare.shared.security.Permission;

import java.util.Set;

public record AccessTokenResult(
        String accessToken,
        long expiresInSeconds,
        Set<Permission> permissions
) {
}

