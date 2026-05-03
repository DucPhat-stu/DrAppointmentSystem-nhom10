package com.healthcare.user.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record DoctorCertificationRequest(
        @NotBlank(message = "Certification name is required")
        String name,
        String issuingAuthority,
        LocalDate issueDate,
        LocalDate expiryDate,
        String documentUrl
) {
}
