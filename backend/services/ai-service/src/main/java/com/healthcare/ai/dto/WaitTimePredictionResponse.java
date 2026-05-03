package com.healthcare.ai.dto;

public record WaitTimePredictionResponse(int estimatedMinutes, String confidence, String explanation) {
}
