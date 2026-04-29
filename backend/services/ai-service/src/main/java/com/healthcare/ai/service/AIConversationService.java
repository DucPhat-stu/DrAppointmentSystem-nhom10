package com.healthcare.ai.service;

import com.healthcare.ai.dto.StructuredAICheckRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AIConversationService {
    private final AIPromptBuilder promptBuilder;
    private final AIClient aiClient;
    private final AIResponseParser parser;
    private final AITextFormatter formatter;
    private final DynamicPromptBuilder dynamicPromptBuilder;

    @Autowired
    public AIConversationService(AIPromptBuilder promptBuilder,
                                 DynamicPromptBuilder dynamicPromptBuilder,
                                 AIClient aiClient,
                                 AIResponseParser parser,
                                 AITextFormatter formatter) {
        this.promptBuilder = promptBuilder;
        this.dynamicPromptBuilder = dynamicPromptBuilder;
        this.aiClient = aiClient;
        this.parser = parser;
        this.formatter = formatter;
    }

    AIConversationService(AIPromptBuilder promptBuilder,
                          AIClient aiClient,
                          AIResponseParser parser,
                          AITextFormatter formatter) {
        this.promptBuilder = promptBuilder;
        this.dynamicPromptBuilder = null;
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
        String prompt = dynamicPromptBuilder == null
                ? promptBuilder.buildStructuredPrompt(request)
                : dynamicPromptBuilder.build(request);
        String rawResponse = aiClient.generate(prompt);
        return formatter.format(parser.parse(rawResponse));
    }

    public String previewStructuredPrompt(StructuredAICheckRequest request) {
        if (dynamicPromptBuilder == null) {
            return promptBuilder.buildStructuredPrompt(request);
        }
        return dynamicPromptBuilder.build(request);
    }
}
