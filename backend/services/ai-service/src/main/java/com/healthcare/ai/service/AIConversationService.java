package com.healthcare.ai.service;

import com.healthcare.ai.dto.StructuredAICheckRequest;
import org.springframework.stereotype.Service;

@Service
public class AIConversationService {
    private final AIPromptBuilder promptBuilder;
    private final AIClient aiClient;
    private final AIResponseParser parser;
    private final AITextFormatter formatter;

    public AIConversationService(AIPromptBuilder promptBuilder,
                                 AIClient aiClient,
                                 AIResponseParser parser,
                                 AITextFormatter formatter) {
        this.promptBuilder = promptBuilder;
        this.aiClient = aiClient;
        this.parser = parser;
        this.formatter = formatter;
    }

    public String checkSymptoms(String text) {
        String prompt = promptBuilder.buildTextPrompt(text);
        String rawResponse = aiClient.generate(prompt);
        return formatter.format(parser.parse(rawResponse));
    }

    public String checkStructuredSymptoms(StructuredAICheckRequest request) {
        String prompt = promptBuilder.buildStructuredPrompt(request);
        String rawResponse = aiClient.generate(prompt);
        return formatter.format(parser.parse(rawResponse));
    }
}
