package com.healthcare.appointment.events;

import com.healthcare.appointment.entity.AppointmentEntity;
import com.healthcare.appointment.entity.AppointmentEventOutboxEntity;
import com.healthcare.appointment.repository.AppointmentEventOutboxJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class OutboxAppointmentEventPublisher implements AppointmentEventPublisher {
    private final AppointmentEventOutboxJpaRepository outboxRepository;

    public OutboxAppointmentEventPublisher(AppointmentEventOutboxJpaRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @Override
    public void publishStatusChanged(String eventName, AppointmentEntity appointment) {
        outboxRepository.save(AppointmentEventOutboxEntity.from(eventName, appointment));
    }
}
