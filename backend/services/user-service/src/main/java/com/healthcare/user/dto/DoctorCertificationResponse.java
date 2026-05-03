package com.healthcare.user.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record DoctorCertificationResponse(
        String id,
        String name,
        String issuingAuthority,
        LocalDate issueDate,
        LocalDate expiryDate,
        String documentUrl,
        OffsetDateTime createdAt
) {
}
