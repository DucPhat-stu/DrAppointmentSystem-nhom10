package com.healthcare.user.dto;

import java.time.LocalDate;

public record UserProfileResponse(
        String id,
        String userId,
        String fullName,
        String email,
        String phone,
        String address,
        LocalDate dateOfBirth,
        String gender,
        String emergencyContact,
        String avatarUrl,
        String specialty,
        String department
) {
}
