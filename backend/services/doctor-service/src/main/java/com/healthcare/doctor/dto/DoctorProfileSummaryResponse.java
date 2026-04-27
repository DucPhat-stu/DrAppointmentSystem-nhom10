package com.healthcare.doctor.dto;

import java.util.UUID;

public record DoctorProfileSummaryResponse(
        UUID userId,
        String fullName,
        String email,
        String specialty,
        String department,
        String avatarUrl
) {
}
