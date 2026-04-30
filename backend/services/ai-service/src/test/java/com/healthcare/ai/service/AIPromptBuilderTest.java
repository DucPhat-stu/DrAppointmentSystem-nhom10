package com.healthcare.ai.service;

import com.healthcare.ai.dto.StructuredAICheckRequest;
import com.healthcare.ai.dto.SymptomDuration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AIPromptBuilderTest {
    private final AIPromptBuilder builder = new AIPromptBuilder(new AIInputSanitizer());

    @Test
    void buildTextPromptSanitizesInput() {
        String prompt = builder.buildTextPrompt("<b>Ho</b> va sot");

        assertThat(prompt).contains("Ho va sot");
        assertThat(prompt).contains("<user_symptoms>");
        assertThat(prompt).doesNotContain("<b>");
    }

    @Test
    void buildStructuredPromptHasNoPlaceholders() {
        String prompt = builder.buildStructuredPrompt(new StructuredAICheckRequest(
                "Ho, sot",
                SymptomDuration.ONE_TO_THREE_DAYS,
                "Met moi"
        ));

        assertThat(prompt).contains("Duration: one to three days");
        assertThat(prompt).contains("<user_symptoms>");
        assertThat(prompt).contains("<user_description>");
        assertThat(prompt).contains("Met moi");
        assertThat(prompt).doesNotContain("{{");
        assertThat(prompt).doesNotContain("}}");
    }
}
