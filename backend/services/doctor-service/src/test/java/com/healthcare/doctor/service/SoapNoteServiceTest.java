package com.healthcare.doctor.service;

import com.healthcare.doctor.dto.DoctorAppointmentOwnershipResponse;
import com.healthcare.doctor.dto.SoapNoteRequest;
import com.healthcare.doctor.entity.SoapNoteAuditLogEntity;
import com.healthcare.doctor.entity.SoapNoteEntity;
import com.healthcare.doctor.repository.SoapNoteAuditLogJpaRepository;
import com.healthcare.doctor.repository.SoapNoteJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SoapNoteServiceTest {
    @Mock
    private SoapNoteJpaRepository soapNoteRepository;

    @Mock
    private SoapNoteAuditLogJpaRepository auditLogRepository;

    @Mock
    private DoctorAppointmentService appointmentService;

    private SoapNoteService service;

    @BeforeEach
    void setUp() {
        service = new SoapNoteService(soapNoteRepository, auditLogRepository, appointmentService, new SoapInputSanitizer());
    }

    @Test
    void saveCreatesSanitizedSoapNoteForOwnedAppointment() {
        UUID doctorId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        when(appointmentService.ownership(doctorId, appointmentId)).thenReturn(
                new DoctorAppointmentOwnershipResponse(appointmentId, doctorId, patientId, "CONFIRMED")
        );
        when(soapNoteRepository.findByAppointmentIdAndDoctorId(appointmentId, doctorId)).thenReturn(Optional.empty());
        when(soapNoteRepository.save(any(SoapNoteEntity.class))).thenAnswer(invocation -> {
            SoapNoteEntity note = invocation.getArgument(0);
            note.setId(UUID.randomUUID());
            return note;
        });

        var response = service.save(doctorId, appointmentId, new SoapNoteRequest(
                "<script>alert(1)</script>headache",
                "BP < 120",
                "stable",
                "follow up"
        ));

        assertThat(response.subjective()).isEqualTo("headache");
        assertThat(response.objective()).isEqualTo("BP &lt; 120");
        verify(auditLogRepository).save(any(SoapNoteAuditLogEntity.class));
    }
}
