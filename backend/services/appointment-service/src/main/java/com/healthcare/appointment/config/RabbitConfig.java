package com.healthcare.appointment.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String APPOINTMENT_EVENTS_EXCHANGE = "appointment.events";

    @Bean
    TopicExchange appointmentEventsExchange() {
        return new TopicExchange(APPOINTMENT_EVENTS_EXCHANGE, true, false);
    }
}
