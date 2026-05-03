package com.healthcare.ai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AIFeedbackRequest(
        @NotNull UUID messageId,
        @Min(1) @Max(5) Integer rating,
        String comment
) {
}
