package com.healthcare.ai.dto;

import java.util.List;

public record AICheckResponse(
        List<String> possibleConditions,
        List<String> symptomsDetected,
        String recommendedSpecialty,
        String advice
) {
}
