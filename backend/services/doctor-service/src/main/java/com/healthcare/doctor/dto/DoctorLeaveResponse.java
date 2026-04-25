package com.healthcare.doctor.dto;

import com.healthcare.doctor.domain.LeaveStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DoctorLeaveResponse(
        UUID id,
        UUID doctorId,
        LocalDate startDate,
        LocalDate endDate,
        LeaveStatus status,
        String rejectionReason,
        UUID decidedBy,
        OffsetDateTime decidedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
