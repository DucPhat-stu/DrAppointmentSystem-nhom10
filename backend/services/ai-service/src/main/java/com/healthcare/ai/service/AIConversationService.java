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
    private final AILocalSymptomAnalyzer localSymptomAnalyzer;

    @Autowired
    public AIConversationService(AIPromptBuilder promptBuilder,
                                 DynamicPromptBuilder dynamicPromptBuilder,
                                 AIClient aiClient,
                                 AIResponseParser parser,
                                 AITextFormatter formatter,
                                 AILocalSymptomAnalyzer localSymptomAnalyzer) {
        this.promptBuilder = promptBuilder;
        this.dynamicPromptBuilder = dynamicPromptBuilder;
        this.aiClient = aiClient;
        this.parser = parser;
        this.formatter = formatter;
        this.localSymptomAnalyzer = localSymptomAnalyzer;
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
        this.localSymptomAnalyzer = new AILocalSymptomAnalyzer();
    }

    public String checkSymptoms(String text) {
        String prompt = promptBuilder.buildTextPrompt(text);
        String rawResponse = aiClient.generate(prompt);
        return formatter.format(parseOrLocalFallback(rawResponse, text));
    }

    public String checkStructuredSymptoms(StructuredAICheckRequest request) {
        String prompt = dynamicPromptBuilder == null
                ? promptBuilder.buildStructuredPrompt(request)
                : dynamicPromptBuilder.build(request);
        String rawResponse = aiClient.generate(prompt);
        String sourceText = "%s %s".formatted(request.symptoms(), request.description() == null ? "" : request.description());
        return formatter.format(parseOrLocalFallback(rawResponse, sourceText));
    }

    public String previewStructuredPrompt(StructuredAICheckRequest request) {
        if (dynamicPromptBuilder == null) {
            return promptBuilder.buildStructuredPrompt(request);
        }
        return dynamicPromptBuilder.build(request);
    }

    private com.healthcare.ai.dto.AICheckResponse parseOrLocalFallback(String rawResponse, String sourceText) {
        com.healthcare.ai.dto.AICheckResponse parsed = parser.parse(rawResponse);
        if (isGenericFallback(parsed)) {
            return localSymptomAnalyzer.analyze(sourceText);
        }
        return parsed;
    }

    private static boolean isGenericFallback(com.healthcare.ai.dto.AICheckResponse response) {
        return response == null
                || response.possibleConditions().contains("Khong xac dinh")
                || response.symptomsDetected().contains("Khong the phan tich");
    }
}
