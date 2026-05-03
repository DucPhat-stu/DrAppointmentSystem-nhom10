package com.healthcare.appointment.scheduler;

import com.healthcare.appointment.domain.AppointmentStatus;
import com.healthcare.appointment.entity.AppointmentEntity;
import com.healthcare.appointment.events.AppointmentEventPublisher;
import com.healthcare.appointment.repository.AppointmentJpaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Component
public class AppointmentReminderScheduler {
    private final AppointmentJpaRepository appointmentRepository;
    private final AppointmentEventPublisher eventPublisher;

    public AppointmentReminderScheduler(AppointmentJpaRepository appointmentRepository,
                                        AppointmentEventPublisher eventPublisher) {
        this.appointmentRepository = appointmentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(cron = "0 */15 * * * *")
    @Transactional
    public void publishDueReminders() {
        OffsetDateTime now = OffsetDateTime.now();
        publish24h(now);
        publish1h(now);
    }

    private void publish24h(OffsetDateTime now) {
        OffsetDateTime from = now.plusHours(23).plusMinutes(45);
        OffsetDateTime to = now.plusHours(24).plusMinutes(15);
        for (AppointmentEntity appointment : appointmentRepository
                .findTop100ByStatusAndReminder24hSentAtIsNullAndScheduledStartBetween(AppointmentStatus.CONFIRMED, from, to)) {
            eventPublisher.publishStatusChanged("APPOINTMENT_REMINDER_24H", appointment);
            appointment.setReminder24hSentAt(now);
        }
    }

    private void publish1h(OffsetDateTime now) {
        OffsetDateTime from = now.plusMinutes(45);
        OffsetDateTime to = now.plusMinutes(75);
        for (AppointmentEntity appointment : appointmentRepository
                .findTop100ByStatusAndReminder1hSentAtIsNullAndScheduledStartBetween(AppointmentStatus.CONFIRMED, from, to)) {
            eventPublisher.publishStatusChanged("APPOINTMENT_REMINDER_1H", appointment);
            appointment.setReminder1hSentAt(now);
        }
    }
}
