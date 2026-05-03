package com.healthcare.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record HealthRiskAlertRequest(@NotBlank String symptoms, Integer age) {
}
