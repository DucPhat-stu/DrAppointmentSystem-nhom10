package com.healthcare.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StructuredAICheckRequest(
        @NotBlank(message = "Symptoms are required")
        @Size(min = 2, max = 180, message = "Symptoms must be 2-180 characters")
        String symptoms,

        @NotNull(message = "Duration is required")
        SymptomDuration duration,

        @Size(max = 300, message = "Description must be at most 300 characters")
        String description
) {
}
