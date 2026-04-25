package com.healthcare.doctor.service;

import com.healthcare.doctor.client.AppointmentServiceClient;
import com.healthcare.doctor.client.UserMedicalRecordClient;
import com.healthcare.doctor.dto.DoctorAppointmentPageResponse;
import com.healthcare.doctor.dto.PatientHistoryResponse;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PatientHistoryService {
    private final AppointmentServiceClient appointmentServiceClient;
    private final UserMedicalRecordClient medicalRecordClient;

    public PatientHistoryService(AppointmentServiceClient appointmentServiceClient,
                                 UserMedicalRecordClient medicalRecordClient) {
        this.appointmentServiceClient = appointmentServiceClient;
        this.medicalRecordClient = medicalRecordClient;
    }

    public PatientHistoryResponse find(UUID doctorId, UUID patientId, int page, int size) {
        DoctorAppointmentPageResponse accessCheck =
                appointmentServiceClient.findDoctorPatientAppointments(doctorId, patientId, 0, 1);
        if (accessCheck.totalElements() == 0) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Patient history not found for doctor");
        }

        DoctorAppointmentPageResponse appointments = page == 0 && size == 1
                ? accessCheck
                : appointmentServiceClient.findDoctorPatientAppointments(doctorId, patientId, page, size);
        var medicalRecords = medicalRecordClient.findPatientMedicalRecords(patientId);

        return new PatientHistoryResponse(
                patientId,
                appointments.content(),
                medicalRecords,
                appointments.page(),
                appointments.size(),
                appointments.totalElements(),
                appointments.totalPages()
        );
    }
}
