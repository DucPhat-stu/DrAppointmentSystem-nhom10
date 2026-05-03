package com.healthcare.notification.dto;

public record NotificationPreferenceRequest(
        Boolean emailEnabled,
        Boolean smsEnabled,
        Boolean pushEnabled,
        Boolean reminder24h,
        Boolean reminder1h,
        Boolean appointmentUpdates
) {
}
