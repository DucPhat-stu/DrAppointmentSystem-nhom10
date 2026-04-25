package com.healthcare.appointment.repository;

import com.healthcare.appointment.entity.AppointmentEventOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AppointmentEventOutboxJpaRepository extends JpaRepository<AppointmentEventOutboxEntity, UUID> {
    List<AppointmentEventOutboxEntity> findTop50ByPublishedAtIsNullAndAttemptsLessThanOrderByCreatedAtAsc(int maxAttempts);
}
