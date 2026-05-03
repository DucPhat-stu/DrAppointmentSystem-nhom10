package com.healthcare.notification.service;

import com.healthcare.notification.dto.NotificationPageResponse;
import com.healthcare.notification.dto.NotificationResponse;
import com.healthcare.notification.entity.NotificationEntity;
import com.healthcare.notification.repository.NotificationJpaRepository;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {
    private final NotificationJpaRepository notificationRepository;
    private final NotificationPreferenceService preferenceService;

    public NotificationService(NotificationJpaRepository notificationRepository,
                               NotificationPreferenceService preferenceService) {
        this.notificationRepository = notificationRepository;
        this.preferenceService = preferenceService;
    }

    @Transactional(readOnly = true)
    public NotificationPageResponse list(UUID recipientId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        var notifications = notificationRepository.findAllByRecipientId(recipientId, pageRequest);
        return new NotificationPageResponse(
                notifications.getContent().stream().map(this::toResponse).toList(),
                notifications.getNumber(),
                notifications.getSize(),
                notifications.getTotalElements(),
                notifications.getTotalPages(),
                notificationRepository.countByRecipientIdAndReadAtIsNull(recipientId)
        );
    }

    @Transactional
    public NotificationResponse markRead(UUID recipientId, UUID notificationId) {
        NotificationEntity notification = notificationRepository.findByIdAndRecipientId(notificationId, recipientId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Notification not found"));
        notification.markRead();
        return toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public NotificationPageResponse markRead(UUID recipientId, List<UUID> notificationIds) {
        List<NotificationEntity> notifications = notificationRepository.findAllByIdInAndRecipientId(notificationIds, recipientId);
        notifications.forEach(NotificationEntity::markRead);
        notificationRepository.saveAll(notifications);
        return list(recipientId, 0, Math.max(notifications.size(), 20));
    }

    @Transactional
    public void create(UUID recipientId,
                       UUID appointmentId,
                       UUID eventId,
                       String eventName,
                       String type,
                       String title,
                       String content) {
        if (!preferenceService.allows(recipientId, eventName)) {
            return;
        }
        if (eventId != null && notificationRepository.existsByEventIdAndRecipientId(eventId, recipientId)) {
            return;
        }
        if (eventId == null && notificationRepository.existsByAppointmentIdAndEventNameAndRecipientId(appointmentId, eventName, recipientId)) {
            return;
        }

        NotificationEntity notification = new NotificationEntity();
        notification.setRecipientId(recipientId);
        notification.setAppointmentId(appointmentId);
        notification.setEventId(eventId);
        notification.setEventName(eventName);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);

        try {
            notificationRepository.save(notification);
        } catch (DataIntegrityViolationException ignored) {
            // Rabbit redelivery can race with an already persisted event; the unique index keeps it idempotent.
        }
    }

    @Transactional
    public int broadcast(List<UUID> recipientIds, UUID eventId, String type, String title, String content) {
        int created = 0;
        for (UUID recipientId : recipientIds) {
            create(recipientId, null, eventId, "BROADCAST", type, title, content);
            created++;
        }
        return created;
    }

    private NotificationResponse toResponse(NotificationEntity entity) {
        return new NotificationResponse(
                entity.getId(),
                entity.getRecipientId(),
                entity.getAppointmentId(),
                entity.getEventId(),
                entity.getEventName(),
                entity.getType(),
                entity.getTitle(),
                entity.getContent(),
                entity.getReadAt() != null,
                entity.getReadAt(),
                entity.getCreatedAt()
        );
    }
}
