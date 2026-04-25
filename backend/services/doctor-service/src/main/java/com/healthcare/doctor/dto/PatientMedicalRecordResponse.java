package com.healthcare.doctor.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record PatientMedicalRecordResponse(
        String id,
        String recordCode,
        String doctorName,
        String department,
        String diseaseSummary,
        String prescription,
        LocalDate visitDate,
        LocalDate appointmentDate,
        OffsetDateTime checkinTime,
        List<String> tests,
        String notes
) {
}
