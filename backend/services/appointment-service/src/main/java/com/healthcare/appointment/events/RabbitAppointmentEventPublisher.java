package com.healthcare.appointment.events;

import com.healthcare.appointment.entity.AppointmentEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RabbitAppointmentEventPublisher implements AppointmentEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(RabbitAppointmentEventPublisher.class);
    private static final String EXCHANGE = "appointment.events";

    private final RabbitTemplate rabbitTemplate;

    public RabbitAppointmentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    @Async
    public void publishStatusChanged(String eventName, AppointmentEntity appointment) {
        Map<String, String> event = Map.of(
                "event", eventName,
                "appointmentId", appointment.getId().toString(),
                "doctorId", appointment.getDoctorId().toString(),
                "patientId", appointment.getPatientId().toString()
        );
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, eventName, event);
        } catch (AmqpException exception) {
            log.warn("Could not publish appointment event {}", eventName, exception);
        }
    }
}
