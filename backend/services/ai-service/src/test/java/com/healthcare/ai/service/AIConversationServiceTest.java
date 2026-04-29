package com.healthcare.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.ai.config.AIClientProperties;
import com.healthcare.ai.config.GeminiProperties;
import com.healthcare.ai.dto.StructuredAICheckRequest;
import com.healthcare.ai.dto.SymptomDuration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AIConversationServiceTest {
    @Test
    void checkSymptomsReturnsFormattedText() {
        AIConversationService service = new AIConversationService(
                new AIPromptBuilder(new AIInputSanitizer()),
                new AIClient(new GeminiProperties(), new AIClientProperties(), new ObjectMapper()),
                new AIResponseParser(new ObjectMapper()),
                new AITextFormatter()
        );

        String result = service.checkSymptoms("Ho va sot trong hai ngay");

        assertThat(result).contains("Cac benh co the:");
        assertThat(result).contains("Luu y:");
    }

    @Test
    void checkStructuredSymptomsReturnsFormattedText() {
        AIConversationService service = new AIConversationService(
                new AIPromptBuilder(new AIInputSanitizer()),
                new AIClient(new GeminiProperties(), new AIClientProperties(), new ObjectMapper()),
                new AIResponseParser(new ObjectMapper()),
                new AITextFormatter()
        );

        String result = service.checkStructuredSymptoms(new StructuredAICheckRequest(
                "Ho va sot",
                SymptomDuration.ONE_TO_THREE_DAYS,
                "Met moi"
        ));

        assertThat(result).contains("Cac benh co the:");
        assertThat(result).contains("Luu y:");
    }
}
