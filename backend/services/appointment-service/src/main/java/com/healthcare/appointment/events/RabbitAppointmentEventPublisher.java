package com.healthcare.appointment.events;

import com.healthcare.appointment.config.RabbitConfig;
import com.healthcare.appointment.entity.AppointmentEventOutboxEntity;
import com.healthcare.appointment.repository.AppointmentEventOutboxJpaRepository;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class RabbitAppointmentEventPublisher {
    private static final int MAX_ATTEMPTS = 10;

    private final RabbitTemplate rabbitTemplate;
    private final AppointmentEventOutboxJpaRepository outboxRepository;

    public RabbitAppointmentEventPublisher(RabbitTemplate rabbitTemplate,
                                           AppointmentEventOutboxJpaRepository outboxRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.outboxRepository = outboxRepository;
    }

    @Scheduled(fixedDelayString = "${app.outbox.appointment-events.fixed-delay-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        for (AppointmentEventOutboxEntity event : outboxRepository
                .findTop50ByPublishedAtIsNullAndAttemptsLessThanOrderByCreatedAtAsc(MAX_ATTEMPTS)) {
            publish(event);
        }
    }

    private void publish(AppointmentEventOutboxEntity event) {
        OffsetDateTime attemptedAt = OffsetDateTime.now();
        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.APPOINTMENT_EVENTS_EXCHANGE,
                    event.getEventName(),
                    event.getPayload()
            );
            event.markPublished(attemptedAt);
        } catch (AmqpException exception) {
            event.markFailed(exception.getMessage(), attemptedAt);
        }
        outboxRepository.save(event);
    }
}
