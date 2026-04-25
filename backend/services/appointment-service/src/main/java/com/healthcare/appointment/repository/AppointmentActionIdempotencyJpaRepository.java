package com.healthcare.appointment.repository;

import com.healthcare.appointment.entity.AppointmentActionIdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppointmentActionIdempotencyJpaRepository extends JpaRepository<AppointmentActionIdempotencyEntity, UUID> {
    Optional<AppointmentActionIdempotencyEntity> findByAppointmentIdAndActionNameAndIdempotencyKey(
            UUID appointmentId,
            String actionName,
            String idempotencyKey
    );
}
