package com.healthcare.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record BroadcastNotificationRequest(
        @NotEmpty(message = "At least one recipient is required")
        List<UUID> recipientIds,

        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Content is required")
        String content,

        String type
) {
}
