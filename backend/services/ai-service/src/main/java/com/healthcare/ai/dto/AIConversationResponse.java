package com.healthcare.ai.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AIConversationResponse(
        UUID id,
        String title,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<AIMessageResponse> messages
) {
}
