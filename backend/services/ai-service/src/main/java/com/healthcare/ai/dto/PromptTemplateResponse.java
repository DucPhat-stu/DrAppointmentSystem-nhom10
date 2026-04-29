package com.healthcare.ai.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PromptTemplateResponse(
        UUID id,
        String name,
        String template,
        List<String> variables,
        boolean active,
        int version,
        Instant createdAt,
        Instant updatedAt
) {
}
