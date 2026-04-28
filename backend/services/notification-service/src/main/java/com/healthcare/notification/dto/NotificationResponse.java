package com.healthcare.notification.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID recipientId,
        UUID appointmentId,
        UUID eventId,
        String eventName,
        String type,
        String title,
        String content,
        boolean read,
        OffsetDateTime readAt,
        OffsetDateTime createdAt
) {
}
