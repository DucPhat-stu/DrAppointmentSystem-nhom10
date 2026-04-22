package com.healthcare.auth.service.login;

import com.healthcare.auth.entity.UserStatus;
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
