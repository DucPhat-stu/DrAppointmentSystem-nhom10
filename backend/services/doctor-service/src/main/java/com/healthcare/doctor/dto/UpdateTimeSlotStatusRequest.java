package com.healthcare.doctor.dto;

import com.healthcare.doctor.domain.TimeSlotStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTimeSlotStatusRequest(
        @NotNull(message = "Status is required")
        TimeSlotStatus status
) {
}
