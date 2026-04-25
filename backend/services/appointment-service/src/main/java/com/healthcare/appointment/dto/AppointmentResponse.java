package com.healthcare.appointment.dto;

import com.healthcare.appointment.domain.AppointmentStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        UUID doctorId,
        UUID patientId,
        UUID slotId,
        OffsetDateTime scheduledStart,
        OffsetDateTime scheduledEnd,
        AppointmentStatus status,
        String reason,
        String cancellationReason
) {
}
