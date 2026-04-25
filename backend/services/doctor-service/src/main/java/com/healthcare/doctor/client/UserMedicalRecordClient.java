package com.healthcare.doctor.client;

import com.healthcare.doctor.dto.PatientMedicalRecordResponse;

import java.util.List;
import java.util.UUID;

public interface UserMedicalRecordClient {
    List<PatientMedicalRecordResponse> findPatientMedicalRecords(UUID patientId);
}
