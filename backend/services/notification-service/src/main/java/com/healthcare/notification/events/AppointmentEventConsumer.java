package com.healthcare.notification.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.notification.config.RabbitConfig;
import com.healthcare.notification.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AppointmentEventConsumer {
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public AppointmentEventConsumer(ObjectMapper objectMapper, NotificationService notificationService) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitConfig.NOTIFICATION_APPOINTMENT_QUEUE)
    public void consume(String payload) throws JsonProcessingException {
        AppointmentEventPayload event = objectMapper.readValue(payload, AppointmentEventPayload.class);
        switch (event.event()) {
            case "APPOINTMENT_REQUESTED" -> {
                notifyPatient(event, "Appointment request submitted", "Your appointment request is waiting for doctor confirmation.");
                notifyDoctor(event, "New appointment request", "A patient requested an appointment and is waiting for your response.");
            }
            case "APPOINTMENT_CONFIRMED" ->
                    notifyPatient(event, "Appointment confirmed", "Your appointment has been confirmed by the doctor.");
            case "APPOINTMENT_REJECTED" ->
                    notifyPatient(event, "Appointment rejected", "Your appointment request was rejected by the doctor.");
            case "APPOINTMENT_CANCELLED" ->
                    notifyPatient(event, "Appointment cancelled", "Your appointment was cancelled by the doctor.");
            case "APPOINTMENT_CANCELLED_BY_PATIENT" -> {
                notifyPatient(event, "Appointment cancelled", "Your appointment cancellation has been recorded.");
                notifyDoctor(event, "Patient cancelled appointment", "A patient cancelled an appointment on your schedule.");
            }
            case "APPOINTMENT_RESCHEDULED" -> {
                notifyPatient(event, "Appointment rescheduled", "Your new appointment time is waiting for doctor confirmation.");
                notifyDoctor(event, "Appointment rescheduled", "A patient requested a new appointment time.");
            }
            case "APPOINTMENT_COMPLETED" ->
                    notifyPatient(event, "Appointment completed", "Your appointment has been marked as completed.");
            default -> {
                // Unknown event types are intentionally ignored to keep consumers forward compatible.
            }
        }
    }

    private void notifyPatient(AppointmentEventPayload event, String title, String content) {
        notificationService.create(event.patientId(), event.appointmentId(), event.eventId(), event.event(), "APPOINTMENT", title, content);
    }

    private void notifyDoctor(AppointmentEventPayload event, String title, String content) {
        notificationService.create(event.doctorId(), event.appointmentId(), event.eventId(), event.event(), "APPOINTMENT", title, content);
    }

    private record AppointmentEventPayload(
            UUID eventId,
            String event,
            UUID appointmentId,
            UUID doctorId,
            UUID patientId
    ) {
    }
}
