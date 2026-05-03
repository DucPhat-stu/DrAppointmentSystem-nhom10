package com.healthcare.ai.dto;

import java.util.List;

public record HealthRiskAlertResponse(String level, List<String> alerts, String nextStep) {
}
