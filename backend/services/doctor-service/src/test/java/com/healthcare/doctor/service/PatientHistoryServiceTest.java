package com.healthcare.doctor.service;

import com.healthcare.doctor.client.AppointmentServiceClient;
import com.healthcare.doctor.client.UserMedicalRecordClient;
import com.healthcare.doctor.dto.DoctorAppointmentPageResponse;
import com.healthcare.doctor.dto.DoctorAppointmentResponse;
import com.healthcare.doctor.dto.PatientMedicalRecordResponse;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientHistoryServiceTest {
    @Mock
    private AppointmentServiceClient appointmentServiceClient;

    @Mock
    private UserMedicalRecordClient medicalRecordClient;

    private PatientHistoryService service;

    @BeforeEach
    void setUp() {
        service = new PatientHistoryService(appointmentServiceClient, medicalRecordClient);
    }

    @Test
    void findReturnsHistoryWhenDoctorHasSeenPatient() {
        UUID doctorId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        DoctorAppointmentResponse appointment = appointment(doctorId, patientId);
        PatientMedicalRecordResponse record = medicalRecord();
        when(appointmentServiceClient.findDoctorPatientAppointments(doctorId, patientId, 0, 1))
                .thenReturn(new DoctorAppointmentPageResponse(List.of(appointment), 0, 1, 2, 2));
        when(appointmentServiceClient.findDoctorPatientAppointments(doctorId, patientId, 0, 20))
                .thenReturn(new DoctorAppointmentPageResponse(List.of(appointment), 0, 20, 2, 1));
        when(medicalRecordClient.findPatientMedicalRecords(patientId)).thenReturn(List.of(record));

        var response = service.find(doctorId, patientId, 0, 20);

        assertThat(response.patientId()).isEqualTo(patientId);
        assertThat(response.appointments()).containsExactly(appointment);
        assertThat(response.medicalRecords()).containsExactly(record);
        assertThat(response.totalAppointments()).isEqualTo(2);
    }

    @Test
    void findRejectsPatientWithoutDoctorAppointment() {
        UUID doctorId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        when(appointmentServiceClient.findDoctorPatientAppointments(doctorId, patientId, 0, 1))
                .thenReturn(new DoctorAppointmentPageResponse(List.of(), 0, 1, 0, 0));

        assertThatThrownBy(() -> service.find(doctorId, patientId, 0, 20))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).getErrorCode())
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        verify(medicalRecordClient, never()).findPatientMedicalRecords(patientId);
    }

    private DoctorAppointmentResponse appointment(UUID doctorId, UUID patientId) {
        return new DoctorAppointmentResponse(
                UUID.randomUUID(),
                doctorId,
                patientId,
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-06-25T08:00:00Z"),
                OffsetDateTime.parse("2026-06-25T09:00:00Z"),
                "COMPLETED",
                null,
                null
        );
    }

    private PatientMedicalRecordResponse medicalRecord() {
        return new PatientMedicalRecordResponse(
                UUID.randomUUID().toString(),
                "000001",
                "Dr. Nguyen",
                "Internal Medicine",
                "Stable",
                "Rest",
                LocalDate.parse("2026-06-25"),
                LocalDate.parse("2026-06-25"),
                OffsetDateTime.parse("2026-06-25T08:00:00Z"),
                List.of("CBC"),
                "Follow up"
        );
    }
}
