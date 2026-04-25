package com.healthcare.doctor.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DoctorLeaveRequest(
        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate
) {
}
