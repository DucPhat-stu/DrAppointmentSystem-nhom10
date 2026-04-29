package com.healthcare.ai.service;

import com.healthcare.ai.dto.StructuredAICheckRequest;
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
    private static final String STRUCTURED_TEMPLATE = """
            Analyze the following structured health symptom intake and provide JSON output.
            Symptoms: %s
            Duration: %s
            Additional description: %s

            Return ONLY valid JSON:
            {
              "possible_conditions": ["condition1", "condition2"],
              "symptoms_detected": ["symptom1", "symptom2"],
              "recommended_specialty": "specialty_name",
              "advice": "brief advice"
            }
            """;

    private final AIInputSanitizer sanitizer;

    public AIPromptBuilder(AIInputSanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    public String buildTextPrompt(String symptomsText) {
        return TEXT_TEMPLATE.formatted(sanitizer.sanitize(symptomsText, 500));
    }

    public String buildStructuredPrompt(StructuredAICheckRequest request) {
        String description = sanitizer.sanitize(request.description(), 300);
        return STRUCTURED_TEMPLATE.formatted(
                sanitizer.sanitize(request.symptoms(), 180),
                request.duration().label(),
                description.isBlank() ? "None" : description
        );
    }
}
