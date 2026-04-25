package com.healthcare.doctor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SoapNoteRequest(
        @NotBlank(message = "Subjective note is required")
        @Size(max = 8000, message = "Subjective note must not exceed 8000 characters")
        String subjective,

        @NotBlank(message = "Objective note is required")
        @Size(max = 8000, message = "Objective note must not exceed 8000 characters")
        String objective,

        @NotBlank(message = "Assessment note is required")
        @Size(max = 8000, message = "Assessment note must not exceed 8000 characters")
        String assessment,

        @NotBlank(message = "Plan note is required")
        @Size(max = 8000, message = "Plan note must not exceed 8000 characters")
        String plan
) {
}
