package com.healthcare.appointment.dto;

import jakarta.validation.constraints.Size;

public record AppointmentActionRequest(
        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason
) {
}
