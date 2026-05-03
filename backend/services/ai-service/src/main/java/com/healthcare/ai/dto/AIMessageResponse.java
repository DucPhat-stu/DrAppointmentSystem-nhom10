package com.healthcare.ai.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AIMessageResponse(UUID id, String role, String content, OffsetDateTime createdAt) {
}
