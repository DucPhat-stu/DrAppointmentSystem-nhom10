package com.healthcare.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record FollowUpSuggestionRequest(@NotBlank String diagnosis) {
}
