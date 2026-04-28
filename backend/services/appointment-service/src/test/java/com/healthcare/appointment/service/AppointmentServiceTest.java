package com.healthcare.appointment.service;

import com.healthcare.appointment.client.DoctorSlotClient;
import com.healthcare.appointment.domain.AppointmentStatus;
import com.healthcare.appointment.dto.AppointmentActionRequest;
import com.healthcare.appointment.dto.CreateAppointmentRequest;
import com.healthcare.appointment.dto.DoctorSlotResponse;
import com.healthcare.appointment.dto.RescheduleAppointmentRequest;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    void createBooksAvailableDoctorSlotForPatient() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();
        when(doctorSlotClient.getSlot(slotId)).thenReturn(new DoctorSlotResponse(
                slotId,
                doctorId,
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-06-25T08:00:00Z"),
                OffsetDateTime.parse("2026-06-25T09:00:00Z"),
                "AVAILABLE"
        ));
        when(appointmentRepository.existsBySlotIdAndStatusIn(slotId, List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED)))
                .thenReturn(false);
        when(appointmentRepository.save(any(AppointmentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(patientId, new CreateAppointmentRequest(doctorId, slotId, "Checkup"));

        assertThat(response.patientId()).isEqualTo(patientId);
        assertThat(response.status()).isEqualTo(AppointmentStatus.PENDING);
        verify(doctorSlotClient).updateSlotStatus(slotId, "BOOKED");
        verify(eventPublisher).publishStatusChanged(eq("APPOINTMENT_REQUESTED"), any(AppointmentEntity.class));
    }

    @Test
    void createRejectsUnavailableSlot() {
        UUID doctorId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();
        when(doctorSlotClient.getSlot(slotId)).thenReturn(new DoctorSlotResponse(
                slotId,
                doctorId,
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-06-25T08:00:00Z"),
                OffsetDateTime.parse("2026-06-25T09:00:00Z"),
                "BOOKED"
        ));

        assertThatThrownBy(() -> service.create(UUID.randomUUID(), new CreateAppointmentRequest(doctorId, slotId, "Checkup")))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).getErrorCode())
                .isEqualTo(ErrorCode.CONFLICT);
        verify(appointmentRepository, never()).save(any());
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

    @Test
    void patientCancelReleasesSlotAndPublishesEvent() {
        UUID patientId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        AppointmentEntity appointment = appointment(UUID.randomUUID(), appointmentId, AppointmentStatus.PENDING);
        appointment.setPatientId(patientId);
        when(appointmentRepository.findByIdAndPatientId(appointmentId, patientId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        var response = service.cancelPatientAppointment(
                patientId,
                appointmentId,
                "patient-key-1",
                new AppointmentActionRequest("No longer needed")
        );

        assertThat(response.status()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(response.cancellationReason()).isEqualTo("No longer needed");
        verify(doctorSlotClient).updateSlotStatus(appointment.getSlotId(), "AVAILABLE");
        verify(eventPublisher).publishStatusChanged("APPOINTMENT_CANCELLED_BY_PATIENT", appointment);
    }

    @Test
    void completeRequiresConfirmedAppointment() {
        UUID doctorId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        AppointmentEntity appointment = appointment(doctorId, appointmentId, AppointmentStatus.CONFIRMED);
        when(appointmentRepository.findByIdAndDoctorId(appointmentId, doctorId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        var response = service.complete(doctorId, appointmentId, "complete-key-1");

        assertThat(response.status()).isEqualTo(AppointmentStatus.COMPLETED);
        verify(eventPublisher).publishStatusChanged("APPOINTMENT_COMPLETED", appointment);
        verify(doctorSlotClient, never()).updateSlotStatus(any(), any());
    }

    @Test
    void patientRescheduleMovesAppointmentToNewSlotAndPendingReview() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        UUID oldSlotId = UUID.randomUUID();
        UUID newSlotId = UUID.randomUUID();
        AppointmentEntity appointment = appointment(doctorId, appointmentId, AppointmentStatus.CONFIRMED);
        appointment.setPatientId(patientId);
        appointment.setSlotId(oldSlotId);
        when(appointmentRepository.findByIdAndPatientId(appointmentId, patientId)).thenReturn(Optional.of(appointment));
        when(doctorSlotClient.getSlot(newSlotId)).thenReturn(new DoctorSlotResponse(
                newSlotId,
                doctorId,
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-06-26T10:00:00Z"),
                OffsetDateTime.parse("2026-06-26T11:00:00Z"),
                "AVAILABLE"
        ));
        when(appointmentRepository.existsBySlotIdAndStatusIn(newSlotId, List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED)))
                .thenReturn(false);
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        var response = service.reschedulePatientAppointment(
                patientId,
                appointmentId,
                "reschedule-key-1",
                new RescheduleAppointmentRequest(newSlotId, "Need a later time")
        );

        assertThat(response.slotId()).isEqualTo(newSlotId);
        assertThat(response.status()).isEqualTo(AppointmentStatus.PENDING);
        assertThat(response.reason()).isEqualTo("Need a later time");
        verify(doctorSlotClient).updateSlotStatus(newSlotId, "BOOKED");
        verify(doctorSlotClient).updateSlotStatus(oldSlotId, "AVAILABLE");
        verify(eventPublisher).publishStatusChanged("APPOINTMENT_RESCHEDULED", appointment);
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
