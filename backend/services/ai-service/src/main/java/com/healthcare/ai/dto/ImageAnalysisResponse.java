package com.healthcare.ai.dto;

public record ImageAnalysisResponse(
        String modality,
        String finding,
        String recommendation,
        double confidence
) {
}
