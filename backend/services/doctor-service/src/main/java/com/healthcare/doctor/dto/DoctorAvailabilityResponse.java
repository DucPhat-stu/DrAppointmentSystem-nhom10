package com.healthcare.doctor.dto;

import java.time.LocalDate;
import java.util.UUID;

public record DoctorAvailabilityResponse(
        UUID doctorId,
        LocalDate date,
        long availableSlots
) {
}
