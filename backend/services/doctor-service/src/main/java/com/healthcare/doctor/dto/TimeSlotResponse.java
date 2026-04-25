package com.healthcare.doctor.dto;

import com.healthcare.doctor.domain.TimeSlotStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TimeSlotResponse(
        UUID id,
        UUID scheduleId,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        TimeSlotStatus status
) {
}
