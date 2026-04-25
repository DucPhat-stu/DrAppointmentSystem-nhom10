package com.healthcare.doctor.dto;

import java.util.UUID;

public record DoctorAppointmentOwnershipResponse(
        UUID appointmentId,
        UUID doctorId,
        UUID patientId,
        String status
) {
}
