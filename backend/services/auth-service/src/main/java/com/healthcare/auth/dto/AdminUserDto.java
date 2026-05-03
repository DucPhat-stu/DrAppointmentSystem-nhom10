package com.healthcare.auth.dto;

import com.healthcare.auth.entity.UserStatus;
import com.healthcare.shared.security.Role;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminUserDto(
        UUID id,
        String email,
        String fullName,
        String phone,
        Role role,
        UserStatus status,
        Integer failedLoginAttempts,
        OffsetDateTime lastLoginAt,
        OffsetDateTime createdAt
) {
}
