package com.healthcare.doctor.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SoapNoteResponse(
        UUID id,
        UUID appointmentId,
        UUID doctorId,
        UUID patientId,
        String subjective,
        String objective,
        String assessment,
        String plan,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
