package com.healthcare.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.ai.config.AIClientProperties;
import com.healthcare.ai.config.GeminiProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AIConversationServiceTest {
    @Test
    void checkSymptomsReturnsFormattedText() {
        AIConversationService service = new AIConversationService(
                new AIPromptBuilder(),
                new AIClient(new GeminiProperties(), new AIClientProperties(), new ObjectMapper()),
                new AIResponseParser(new ObjectMapper()),
                new AITextFormatter()
        );

        String result = service.checkSymptoms("Ho va sot trong hai ngay");

        assertThat(result).contains("Cac benh co the:");
        assertThat(result).contains("Luu y:");
    }
}
