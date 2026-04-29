package com.healthcare.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.ai.dto.PromptTemplateRequest;
import com.healthcare.ai.repository.PromptTemplateJpaRepository;
import com.healthcare.shared.common.exception.ApiException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class PromptTemplateServiceTest {
    @Test
    void rejectsTemplateWhenVariablesDoNotMatchPlaceholders() {
        PromptTemplateService service = new PromptTemplateService(
                mock(PromptTemplateJpaRepository.class),
                new ObjectMapper()
        );

        PromptTemplateRequest request = new PromptTemplateRequest(
                "Bad",
                "Symptoms: {{symptoms}}",
                List.of("symptoms", "duration")
        );

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Template variables");
    }
}
