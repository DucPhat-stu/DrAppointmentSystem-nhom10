package com.healthcare.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Request DTO for doctors to create a medical record.
 */
public record CreateMedicalRecordRequest(
        @NotBlank(message = "Patient user ID is required")
        String patientUserId,

        @NotBlank(message = "Disease summary is required")
        @Size(max = 500, message = "Disease summary must not exceed 500 characters")
        String diseaseSummary,

        @NotBlank(message = "Department is required")
        String department,

        String prescription,

        @NotNull(message = "Visit date is required")
        LocalDate visitDate,

        LocalDate appointmentDate,

        OffsetDateTime checkinTime,

        List<String> tests,

        String notes
) {
}
