package com.healthcare.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String APPOINTMENT_EVENTS_EXCHANGE = "appointment.events";
    public static final String NOTIFICATION_APPOINTMENT_QUEUE = "notification.appointment-events";

    @Bean
    TopicExchange appointmentEventsExchange() {
        return new TopicExchange(APPOINTMENT_EVENTS_EXCHANGE, true, false);
    }

    @Bean
    Queue notificationAppointmentQueue() {
        return new Queue(NOTIFICATION_APPOINTMENT_QUEUE, true);
    }

    @Bean
    Binding appointmentRequestedBinding(Queue notificationAppointmentQueue, TopicExchange appointmentEventsExchange) {
        return BindingBuilder.bind(notificationAppointmentQueue)
                .to(appointmentEventsExchange)
                .with("APPOINTMENT_REQUESTED");
    }

    @Bean
    Binding appointmentConfirmedBinding(Queue notificationAppointmentQueue, TopicExchange appointmentEventsExchange) {
        return BindingBuilder.bind(notificationAppointmentQueue)
                .to(appointmentEventsExchange)
                .with("APPOINTMENT_CONFIRMED");
    }

    @Bean
    Binding appointmentRejectedBinding(Queue notificationAppointmentQueue, TopicExchange appointmentEventsExchange) {
        return BindingBuilder.bind(notificationAppointmentQueue)
                .to(appointmentEventsExchange)
                .with("APPOINTMENT_REJECTED");
    }

    @Bean
    Binding appointmentCancelledBinding(Queue notificationAppointmentQueue, TopicExchange appointmentEventsExchange) {
        return BindingBuilder.bind(notificationAppointmentQueue)
                .to(appointmentEventsExchange)
                .with("APPOINTMENT_CANCELLED");
    }

    @Bean
    Binding appointmentCancelledByPatientBinding(Queue notificationAppointmentQueue, TopicExchange appointmentEventsExchange) {
        return BindingBuilder.bind(notificationAppointmentQueue)
                .to(appointmentEventsExchange)
                .with("APPOINTMENT_CANCELLED_BY_PATIENT");
    }

    @Bean
    Binding appointmentRescheduledBinding(Queue notificationAppointmentQueue, TopicExchange appointmentEventsExchange) {
        return BindingBuilder.bind(notificationAppointmentQueue)
                .to(appointmentEventsExchange)
                .with("APPOINTMENT_RESCHEDULED");
    }

    @Bean
    Binding appointmentCompletedBinding(Queue notificationAppointmentQueue, TopicExchange appointmentEventsExchange) {
        return BindingBuilder.bind(notificationAppointmentQueue)
                .to(appointmentEventsExchange)
                .with("APPOINTMENT_COMPLETED");
    }
}
