package com.healthcare.appointment.repository;

import com.healthcare.appointment.entity.AppointmentSlotSyncJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentSlotSyncJobJpaRepository extends JpaRepository<AppointmentSlotSyncJobEntity, UUID> {
    List<AppointmentSlotSyncJobEntity> findTop50ByProcessedAtIsNullAndAttemptsLessThanAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
            int maxAttempts,
            OffsetDateTime now
    );
}
