package com.healthcare.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.ai.dto.AICheckResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AIResponseParserTest {
    private final AIResponseParser parser = new AIResponseParser(new ObjectMapper());

    @Test
    void parseValidJson() {
        String raw = """
                {
                  "possible_conditions": ["Cam cum", "Viem hong"],
                  "symptoms_detected": ["Ho", "Sot"],
                  "recommended_specialty": "Noi tong quat",
                  "advice": "Uong nuoc am va theo doi trieu chung trong 24 gio."
                }
                """;

        AICheckResponse response = parser.parse(raw);

        assertThat(response.possibleConditions()).containsExactly("Cam cum", "Viem hong");
        assertThat(response.symptomsDetected()).containsExactly("Ho", "Sot");
        assertThat(response.recommendedSpecialty()).isEqualTo("Noi tong quat");
    }

    @Test
    void parseMarkdownWrappedJson() {
        String raw = """
                ```json
                {"possible_conditions":["Cam"],"symptoms_detected":["Ho"],"recommended_specialty":"Noi tong quat","advice":"Nen nghi ngoi va theo doi them."}
                ```
                """;

        AICheckResponse response = parser.parse(raw);

        assertThat(response.possibleConditions()).containsExactly("Cam");
    }

    @Test
    void invalidJsonReturnsFallback() {
        AICheckResponse response = parser.parse("not-json");

        assertThat(response.recommendedSpecialty()).isEqualTo("Noi tong quat");
        assertThat(response.advice()).contains("Vui long");
    }

    @Test
    void missingFieldsReturnsFallback() {
        AICheckResponse response = parser.parse("{\"possible_conditions\":[]}");

        assertThat(response.symptomsDetected()).containsExactly("Khong the phan tich");
    }

    @Test
    void arraysAreLimited() {
        String raw = """
                {
                  "possible_conditions": ["1","2","3","4","5","6"],
                  "symptoms_detected": ["1","2","3","4","5","6","7","8","9","10","11"],
                  "recommended_specialty": "Noi tong quat",
                  "advice": "Day la loi khuyen co du do dai de hop le."
                }
                """;

        AICheckResponse response = parser.parse(raw);

        assertThat(response.possibleConditions()).hasSize(5);
        assertThat(response.symptomsDetected()).hasSize(10);
    }
}
