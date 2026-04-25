package com.healthcare.doctor.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ScheduleRequest(
        @NotNull(message = "Schedule date is required")
        LocalDate date
) {
}
