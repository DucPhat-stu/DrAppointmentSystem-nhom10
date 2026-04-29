package com.healthcare.ai.service;

import com.healthcare.ai.dto.StructuredAICheckRequest;
import com.healthcare.ai.entity.PromptTemplateEntity;
import com.healthcare.ai.repository.PromptTemplateJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class DynamicPromptBuilder {
    private final PromptTemplateJpaRepository repository;
    private final AIPromptBuilder fallbackBuilder;
    private final AIInputSanitizer sanitizer;

    public DynamicPromptBuilder(PromptTemplateJpaRepository repository,
                                AIPromptBuilder fallbackBuilder,
                                AIInputSanitizer sanitizer) {
        this.repository = repository;
        this.fallbackBuilder = fallbackBuilder;
        this.sanitizer = sanitizer;
    }

    public String build(StructuredAICheckRequest request) {
        return repository.findFirstByActiveTrue()
                .map(template -> render(template, request))
                .orElseGet(() -> fallbackBuilder.buildStructuredPrompt(request));
    }

    private String render(PromptTemplateEntity template, StructuredAICheckRequest request) {
        String description = sanitizer.sanitize(request.description(), 300);
        String result = template.getTemplate()
                .replace("{{ symptoms }}", sanitizer.sanitize(request.symptoms(), 180))
                .replace("{{symptoms}}", sanitizer.sanitize(request.symptoms(), 180))
                .replace("{{ duration }}", request.duration().label())
                .replace("{{duration}}", request.duration().label())
                .replace("{{ description }}", description.isBlank() ? "None" : description)
                .replace("{{description}}", description.isBlank() ? "None" : description);

        if (result.contains("{{") || result.contains("}}")) {
            return fallbackBuilder.buildStructuredPrompt(request);
        }

        return result;
    }
}
