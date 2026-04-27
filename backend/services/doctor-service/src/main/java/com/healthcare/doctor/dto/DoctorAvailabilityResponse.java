package com.healthcare.doctor.dto;

import java.time.LocalDate;
import java.util.UUID;

public record DoctorAvailabilityResponse(
        UUID doctorId,
        String fullName,
        String specialty,
        String department,
        String avatarUrl,
        LocalDate date,
        long availableSlots
) {
}
