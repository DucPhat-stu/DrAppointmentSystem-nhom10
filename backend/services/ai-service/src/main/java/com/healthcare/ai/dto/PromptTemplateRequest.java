package com.healthcare.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PromptTemplateRequest(
        @NotBlank(message = "Template name is required")
        @Size(max = 255, message = "Template name must be at most 255 characters")
        String name,

        @NotBlank(message = "Template body is required")
        @Size(max = 6000, message = "Template body must be at most 6000 characters")
        String template,

        @NotEmpty(message = "At least one variable is required")
        List<@NotBlank(message = "Variable must not be blank") String> variables
) {
}
