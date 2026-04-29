package com.healthcare.ai.service;

import com.healthcare.ai.dto.StructuredAICheckRequest;
import com.healthcare.ai.dto.SymptomDuration;
import com.healthcare.ai.entity.PromptTemplateEntity;
import com.healthcare.ai.repository.PromptTemplateJpaRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DynamicPromptBuilderTest {
    @Test
    void buildUsesActiveTemplate() {
        PromptTemplateEntity entity = new PromptTemplateEntity();
        entity.setTemplate("Symptoms={{symptoms}} Duration={{duration}} Description={{description}}");
        PromptTemplateJpaRepository repository = mock(PromptTemplateJpaRepository.class);
        when(repository.findFirstByActiveTrue()).thenReturn(Optional.of(entity));

        DynamicPromptBuilder builder = new DynamicPromptBuilder(
                repository,
                new AIPromptBuilder(new AIInputSanitizer()),
                new AIInputSanitizer()
        );

        String prompt = builder.build(new StructuredAICheckRequest(
                "Ho",
                SymptomDuration.LESS_THAN_ONE_DAY,
                "Dau hong"
        ));

        assertThat(prompt).isEqualTo("Symptoms=Ho Duration=less than one day Description=Dau hong");
    }

    @Test
    void buildFallsBackWhenUnresolvedPlaceholderRemains() {
        PromptTemplateEntity entity = new PromptTemplateEntity();
        entity.setTemplate("Symptoms={{symptoms}} Unknown={{unknown}}");
        PromptTemplateJpaRepository repository = mock(PromptTemplateJpaRepository.class);
        when(repository.findFirstByActiveTrue()).thenReturn(Optional.of(entity));

        DynamicPromptBuilder builder = new DynamicPromptBuilder(
                repository,
                new AIPromptBuilder(new AIInputSanitizer()),
                new AIInputSanitizer()
        );

        String prompt = builder.build(new StructuredAICheckRequest(
                "Ho",
                SymptomDuration.LESS_THAN_ONE_DAY,
                ""
        ));

        assertThat(prompt).contains("Return ONLY valid JSON");
        assertThat(prompt).doesNotContain("{{unknown}}");
    }
}
