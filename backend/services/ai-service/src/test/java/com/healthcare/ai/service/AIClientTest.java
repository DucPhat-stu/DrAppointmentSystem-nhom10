package com.healthcare.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.ai.config.AIClientProperties;
import com.healthcare.ai.config.GeminiProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AIClientTest {
    @Test
    void generateReturnsFallbackWhenApiKeyMissing() {
        GeminiProperties geminiProperties = new GeminiProperties();
        geminiProperties.setApiKey("");
        AIClientProperties clientProperties = new AIClientProperties();

        AIClient client = new AIClient(geminiProperties, clientProperties, new ObjectMapper());

        String result = client.generate("Analyze symptoms");

        assertThat(result).contains("possible_conditions");
        assertThat(result).contains("recommended_specialty");
    }
}
