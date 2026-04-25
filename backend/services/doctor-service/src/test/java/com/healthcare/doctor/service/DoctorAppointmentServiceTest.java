package com.healthcare.doctor.service;

import com.healthcare.doctor.client.AppointmentServiceClient;
import com.healthcare.doctor.dto.DoctorAppointmentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorAppointmentServiceTest {
    @Mock
    private AppointmentServiceClient appointmentServiceClient;

    private DoctorAppointmentService service;

    @BeforeEach
    void setUp() {
        service = new DoctorAppointmentService(appointmentServiceClient);
    }

    @Test
    void confirmDelegatesToAppointmentServiceClient() {
        UUID doctorId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        DoctorAppointmentResponse expected = new DoctorAppointmentResponse(
                appointmentId,
                doctorId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-06-25T08:00:00Z"),
                OffsetDateTime.parse("2026-06-25T09:00:00Z"),
                "CONFIRMED",
                null,
                null
        );
        when(appointmentServiceClient.confirm(doctorId, appointmentId, "key-1")).thenReturn(expected);

        DoctorAppointmentResponse response = service.confirm(doctorId, appointmentId, "key-1");

        assertThat(response.status()).isEqualTo("CONFIRMED");
    }
}
