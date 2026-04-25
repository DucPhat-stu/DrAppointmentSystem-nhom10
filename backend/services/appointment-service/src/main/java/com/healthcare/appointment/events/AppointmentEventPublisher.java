package com.healthcare.appointment.events;

import com.healthcare.appointment.entity.AppointmentEntity;

public interface AppointmentEventPublisher {
    void publishStatusChanged(String eventName, AppointmentEntity appointment);
}
