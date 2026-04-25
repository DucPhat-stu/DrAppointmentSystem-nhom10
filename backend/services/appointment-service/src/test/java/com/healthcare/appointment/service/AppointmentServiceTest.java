package com.healthcare.appointment.service;

import com.healthcare.appointment.client.DoctorSlotClient;
import com.healthcare.appointment.domain.AppointmentStatus;
import com.healthcare.appointment.dto.AppointmentActionRequest;
import com.healthcare.appointment.entity.AppointmentEntity;
import com.healthcare.appointment.events.AppointmentEventPublisher;
import com.healthcare.appointment.repository.AppointmentActionIdempotencyJpaRepository;
import com.healthcare.appointment.repository.AppointmentJpaRepository;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {
    @Mock
    private AppointmentJpaRepository appointmentRepository;

    @Mock
    private AppointmentActionIdempotencyJpaRepository idempotencyRepository;

    @Mock
    private AppointmentEventPublisher eventPublisher;

    @Mock
    private DoctorSlotClient doctorSlotClient;

    private AppointmentService service;

    @BeforeEach
    void setUp() {
        service = new AppointmentService(appointmentRepository, idempotencyRepository, eventPublisher, doctorSlotClient);
    }

    @Test
    void confirmMovesPendingAppointmentToConfirmedAndPublishesEvent() {
        UUID doctorId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        AppointmentEntity appointment = appointment(doctorId, appointmentId, AppointmentStatus.PENDING);
        when(appointmentRepository.findByIdAndDoctorId(appointmentId, doctorId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        var response = service.confirm(doctorId, appointmentId, "key-1");

        assertThat(response.status()).isEqualTo(AppointmentStatus.CONFIRMED);
        verify(eventPublisher).publishStatusChanged("APPOINTMENT_CONFIRMED", appointment);
        verify(doctorSlotClient).updateSlotStatus(appointment.getSlotId(), "BOOKED");
    }

    @Test
    void confirmIsIdempotentWhenAppointmentAlreadyConfirmed() {
        UUID doctorId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        AppointmentEntity appointment = appointment(doctorId, appointmentId, AppointmentStatus.CONFIRMED);
        when(appointmentRepository.findByIdAndDoctorId(appointmentId, doctorId)).thenReturn(Optional.of(appointment));

        var response = service.confirm(doctorId, appointmentId, "key-1");

        assertThat(response.status()).isEqualTo(AppointmentStatus.CONFIRMED);
        verify(appointmentRepository, never()).save(any());
        verify(eventPublisher, never()).publishStatusChanged(any(), any());
        verify(doctorSlotClient, never()).updateSlotStatus(any(), any());
    }

    @Test
    void rejectRequiresPendingState() {
        UUID doctorId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        AppointmentEntity appointment = appointment(doctorId, appointmentId, AppointmentStatus.CONFIRMED);
        when(appointmentRepository.findByIdAndDoctorId(appointmentId, doctorId)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.reject(doctorId, appointmentId, "key-1", new AppointmentActionRequest("No slot")))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).getErrorCode())
                .isEqualTo(ErrorCode.CONFLICT);
    }

    private AppointmentEntity appointment(UUID doctorId, UUID appointmentId, AppointmentStatus status) {
        AppointmentEntity appointment = new AppointmentEntity();
        appointment.setId(appointmentId);
        appointment.setDoctorId(doctorId);
        appointment.setPatientId(UUID.randomUUID());
        appointment.setSlotId(UUID.randomUUID());
        appointment.setScheduledStart(OffsetDateTime.parse("2026-06-25T08:00:00Z"));
        appointment.setScheduledEnd(OffsetDateTime.parse("2026-06-25T09:00:00Z"));
        appointment.setStatus(status);
        return appointment;
    }
}
