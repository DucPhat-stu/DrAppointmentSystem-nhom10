package com.healthcare.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record DoctorRecommendationRequest(@NotBlank String symptoms) {
}
