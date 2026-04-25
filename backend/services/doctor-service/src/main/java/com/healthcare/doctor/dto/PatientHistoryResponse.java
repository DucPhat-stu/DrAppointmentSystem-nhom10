package com.healthcare.doctor.dto;

import java.util.List;
import java.util.UUID;

public record PatientHistoryResponse(
        UUID patientId,
        List<DoctorAppointmentResponse> appointments,
        List<PatientMedicalRecordResponse> medicalRecords,
        int page,
        int size,
        long totalAppointments,
        int totalAppointmentPages
) {
}
