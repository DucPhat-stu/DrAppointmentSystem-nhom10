package com.healthcare.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AICheckRequest(
        @NotBlank(message = "Symptoms text is required")
        @Size(min = 5, max = 500, message = "Symptoms text must be 5-500 characters")
        String text
) {
}
