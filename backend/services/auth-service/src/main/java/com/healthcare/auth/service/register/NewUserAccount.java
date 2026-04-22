package com.healthcare.auth.service.register;

import com.healthcare.auth.entity.UserStatus;
import com.healthcare.shared.security.Role;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NewUserAccount(
        UUID userId,
        String email,
        String passwordHash,
        Role role,
        UserStatus status,
        String fullName,
        String phone,
        OffsetDateTime createdAt
) {
}
