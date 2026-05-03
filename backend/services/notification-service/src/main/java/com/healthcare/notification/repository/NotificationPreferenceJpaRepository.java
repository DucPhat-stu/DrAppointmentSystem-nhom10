package com.healthcare.notification.repository;

import com.healthcare.notification.entity.NotificationPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NotificationPreferenceJpaRepository extends JpaRepository<NotificationPreferenceEntity, UUID> {
    Optional<NotificationPreferenceEntity> findByUserId(UUID userId);
}
