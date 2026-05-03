package com.healthcare.ai.dto;

public record DoctorRecommendationResponse(
        String doctorName,
        String specialty,
        String reason,
        double matchScore
) {
}
