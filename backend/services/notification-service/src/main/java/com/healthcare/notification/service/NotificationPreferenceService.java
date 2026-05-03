package com.healthcare.notification.service;

import com.healthcare.notification.dto.NotificationPreferenceRequest;
import com.healthcare.notification.dto.NotificationPreferenceResponse;
import com.healthcare.notification.entity.NotificationPreferenceEntity;
import com.healthcare.notification.repository.NotificationPreferenceJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class NotificationPreferenceService {
    private final NotificationPreferenceJpaRepository preferenceRepository;

    public NotificationPreferenceService(NotificationPreferenceJpaRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
    }

    @Transactional
    public NotificationPreferenceResponse get(UUID userId) {
        return toResponse(getOrCreate(userId));
    }

    @Transactional
    public NotificationPreferenceResponse update(UUID userId, NotificationPreferenceRequest request) {
        NotificationPreferenceEntity entity = getOrCreate(userId);
        if (request.emailEnabled() != null) entity.setEmailEnabled(request.emailEnabled());
        if (request.smsEnabled() != null) entity.setSmsEnabled(request.smsEnabled());
        if (request.pushEnabled() != null) entity.setPushEnabled(request.pushEnabled());
        if (request.reminder24h() != null) entity.setReminder24h(request.reminder24h());
        if (request.reminder1h() != null) entity.setReminder1h(request.reminder1h());
        if (request.appointmentUpdates() != null) entity.setAppointmentUpdates(request.appointmentUpdates());
        return toResponse(preferenceRepository.save(entity));
    }

    public boolean allows(UUID userId, String eventName) {
        NotificationPreferenceEntity entity = preferenceRepository.findByUserId(userId).orElse(null);
        if (entity == null) return true;
        if ("APPOINTMENT_REMINDER_24H".equals(eventName)) return entity.isReminder24h();
        if ("APPOINTMENT_REMINDER_1H".equals(eventName)) return entity.isReminder1h();
        if (eventName != null && eventName.startsWith("APPOINTMENT_")) return entity.isAppointmentUpdates();
        return true;
    }

    private NotificationPreferenceEntity getOrCreate(UUID userId) {
        return preferenceRepository.findByUserId(userId).orElseGet(() -> {
            NotificationPreferenceEntity entity = new NotificationPreferenceEntity();
            entity.setUserId(userId);
            return preferenceRepository.save(entity);
        });
    }

    private NotificationPreferenceResponse toResponse(NotificationPreferenceEntity entity) {
        return new NotificationPreferenceResponse(
                entity.isEmailEnabled(),
                entity.isSmsEnabled(),
                entity.isPushEnabled(),
                entity.isReminder24h(),
                entity.isReminder1h(),
                entity.isAppointmentUpdates()
        );
    }
}
