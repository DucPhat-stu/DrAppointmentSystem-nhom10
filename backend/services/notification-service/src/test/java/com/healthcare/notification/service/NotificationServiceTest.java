package com.healthcare.notification.service;

import com.healthcare.notification.entity.NotificationEntity;
import com.healthcare.notification.repository.NotificationJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock
    private NotificationJpaRepository notificationRepository;

    private NotificationService service;

    @BeforeEach
    void setUp() {
        service = new NotificationService(notificationRepository);
    }

    @Test
    void createUsesEventIdForIdempotency() {
        UUID recipientId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        when(notificationRepository.existsByEventIdAndRecipientId(eventId, recipientId)).thenReturn(false);

        service.create(recipientId, appointmentId, eventId, "APPOINTMENT_RESCHEDULED", "APPOINTMENT", "Rescheduled", "Changed");

        ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getEventId()).isEqualTo(eventId);
        assertThat(captor.getValue().getAppointmentId()).isEqualTo(appointmentId);
    }

    @Test
    void createSkipsDuplicateEventIdForSameRecipient() {
        UUID recipientId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        when(notificationRepository.existsByEventIdAndRecipientId(eventId, recipientId)).thenReturn(true);

        service.create(recipientId, appointmentId, eventId, "APPOINTMENT_RESCHEDULED", "APPOINTMENT", "Rescheduled", "Changed");

        verify(notificationRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createAllowsRepeatedRescheduleEventsWithDifferentEventIds() {
        UUID recipientId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        UUID firstEventId = UUID.randomUUID();
        UUID secondEventId = UUID.randomUUID();
        when(notificationRepository.existsByEventIdAndRecipientId(firstEventId, recipientId)).thenReturn(false);
        when(notificationRepository.existsByEventIdAndRecipientId(secondEventId, recipientId)).thenReturn(false);

        service.create(recipientId, appointmentId, firstEventId, "APPOINTMENT_RESCHEDULED", "APPOINTMENT", "Rescheduled", "Changed once");
        service.create(recipientId, appointmentId, secondEventId, "APPOINTMENT_RESCHEDULED", "APPOINTMENT", "Rescheduled", "Changed twice");

        verify(notificationRepository, times(2)).save(org.mockito.ArgumentMatchers.any(NotificationEntity.class));
    }
}
