package com.healthcare.ai.service;

import org.springframework.stereotype.Component;

@Component
public class AIPromptBuilder {
    private static final String TEXT_TEMPLATE = """
            Analyze the following health symptoms and provide JSON output.
            Symptoms: %s

            Return ONLY valid JSON:
            {
              "possible_conditions": ["condition1", "condition2"],
              "symptoms_detected": ["symptom1", "symptom2"],
              "recommended_specialty": "specialty_name",
              "advice": "brief advice"
            }
            """;

    public String buildTextPrompt(String symptomsText) {
        return TEXT_TEMPLATE.formatted(sanitize(symptomsText));
    }

    String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", " ")
                .replaceAll("\\s+", " ");
    }
}
