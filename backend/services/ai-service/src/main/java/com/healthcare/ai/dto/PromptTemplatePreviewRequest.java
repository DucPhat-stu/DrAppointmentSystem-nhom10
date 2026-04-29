package com.healthcare.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PromptTemplatePreviewRequest(
        @NotBlank(message = "Template body is required")
        @Size(max = 6000, message = "Template body must be at most 6000 characters")
        String template,

        @NotBlank(message = "Symptoms are required")
        String symptoms,

        @NotNull(message = "Duration is required")
        SymptomDuration duration,

        String description
) {
}
