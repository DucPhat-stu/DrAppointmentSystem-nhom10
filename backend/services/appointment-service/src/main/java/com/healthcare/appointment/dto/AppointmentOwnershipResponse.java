package com.healthcare.appointment.dto;

import com.healthcare.appointment.domain.AppointmentStatus;

import java.util.UUID;

public record AppointmentOwnershipResponse(
        UUID appointmentId,
        UUID doctorId,
        UUID patientId,
        AppointmentStatus status
) {
}
