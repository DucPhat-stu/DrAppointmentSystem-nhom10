package com.healthcare.ai.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AILocalSymptomAnalyzerTest {
    private final AILocalSymptomAnalyzer analyzer = new AILocalSymptomAnalyzer();

    @Test
    void analyzesCoughAndFeverWithoutReturningGenericFallback() {
        var response = analyzer.analyze("Ho, sốt cao");

        assertThat(response.possibleConditions()).contains("Nhiem trung hoac cam/cum");
        assertThat(response.symptomsDetected()).contains("Ho/dau hong", "Sot");
        assertThat(response.recommendedSpecialty()).isNotEqualTo("Khong xac dinh");
    }
}
