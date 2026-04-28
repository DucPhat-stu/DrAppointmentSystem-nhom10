package com.healthcare.notification.repository;

import com.healthcare.notification.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, UUID> {
    Page<NotificationEntity> findAllByRecipientId(UUID recipientId, Pageable pageable);

    Optional<NotificationEntity> findByIdAndRecipientId(UUID id, UUID recipientId);

    List<NotificationEntity> findAllByIdInAndRecipientId(Collection<UUID> ids, UUID recipientId);

    long countByRecipientIdAndReadAtIsNull(UUID recipientId);

    boolean existsByAppointmentIdAndEventNameAndRecipientId(UUID appointmentId, String eventName, UUID recipientId);
}
