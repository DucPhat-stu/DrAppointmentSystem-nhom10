package com.healthcare.doctor.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AvailableSlotResponse(
        UUID id,
        UUID doctorId,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        String status
) {
}
