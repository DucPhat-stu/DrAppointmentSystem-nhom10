package com.healthcare.notification.dto;

public record NotificationPreferenceResponse(
        boolean emailEnabled,
        boolean smsEnabled,
        boolean pushEnabled,
        boolean reminder24h,
        boolean reminder1h,
        boolean appointmentUpdates
) {
}
