package com.healthcare.doctor.dto;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TimeSlotRequest(
        @NotNull(message = "Schedule id is required")
        UUID scheduleId,

        @NotNull(message = "Start time is required")
        OffsetDateTime startTime,

        @NotNull(message = "End time is required")
        OffsetDateTime endTime
) {
}
