package com.healthcare.appointment.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DoctorSlotResponse(
        UUID id,
        UUID doctorId,
        UUID scheduleId,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        String status
) {
}
