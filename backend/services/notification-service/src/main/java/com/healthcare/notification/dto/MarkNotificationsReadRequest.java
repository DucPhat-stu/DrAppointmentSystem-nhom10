package com.healthcare.notification.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record MarkNotificationsReadRequest(
        @NotEmpty(message = "Notification IDs are required")
        List<UUID> ids
) {
}
