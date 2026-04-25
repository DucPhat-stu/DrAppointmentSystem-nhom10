package com.healthcare.doctor.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DoctorAppointmentResponse(
        UUID id,
        UUID doctorId,
        UUID patientId,
        UUID slotId,
        OffsetDateTime scheduledStart,
        OffsetDateTime scheduledEnd,
        String status,
        String reason,
        String cancellationReason
) {
}
