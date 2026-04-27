package com.healthcare.appointment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateAppointmentRequest(
        @NotNull(message = "Doctor id is required")
        UUID doctorId,

        @NotNull(message = "Slot id is required")
        UUID slotId,

        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason
) {
}
