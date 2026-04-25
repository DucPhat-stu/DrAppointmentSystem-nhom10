package com.healthcare.doctor.dto;

import jakarta.validation.constraints.Size;

public record LeaveDecisionRequest(
        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason
) {
}
