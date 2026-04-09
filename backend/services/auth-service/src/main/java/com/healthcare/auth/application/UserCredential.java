package com.healthcare.auth.application;

import com.healthcare.auth.domain.UserStatus;
import com.healthcare.shared.security.Role;

import java.util.UUID;

public record UserCredential(
        UUID userId,
        String email,
        String passwordHash,
        Role role,
        UserStatus status
) {
}

