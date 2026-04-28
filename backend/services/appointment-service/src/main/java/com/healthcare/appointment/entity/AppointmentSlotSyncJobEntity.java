package com.healthcare.appointment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointment_slot_sync_jobs")
public class AppointmentSlotSyncJobEntity {
    @Id
    private UUID id;

    @Column(name = "appointment_id", nullable = false)
    private UUID appointmentId;

    @Column(name = "slot_id", nullable = false)
    private UUID slotId;

    @Column(nullable = false, length = 40)
    private String operation;

    @Column(name = "target_status", nullable = false, length = 40)
    private String targetStatus;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "next_attempt_at", nullable = false)
    private OffsetDateTime nextAttemptAt;

    @Column(name = "last_attempt_at")
    private OffsetDateTime lastAttemptAt;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    public static AppointmentSlotSyncJobEntity create(UUID appointmentId,
                                                      UUID slotId,
                                                      String operation,
                                                      String targetStatus) {
        AppointmentSlotSyncJobEntity entity = new AppointmentSlotSyncJobEntity();
        entity.id = UUID.randomUUID();
        entity.appointmentId = appointmentId;
        entity.slotId = slotId;
        entity.operation = operation;
        entity.targetStatus = targetStatus;
        OffsetDateTime now = OffsetDateTime.now();
        entity.createdAt = now;
        entity.nextAttemptAt = now;
        return entity;
    }

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = now;
        }
        if (nextAttemptAt == null) {
            nextAttemptAt = now;
        }
    }

    public void markProcessed(OffsetDateTime processedAt) {
        this.processedAt = processedAt;
        this.lastAttemptAt = processedAt;
        this.lastError = null;
    }

    public void markFailed(String error, OffsetDateTime attemptedAt) {
        this.attempts++;
        this.lastAttemptAt = attemptedAt;
        this.nextAttemptAt = attemptedAt.plusSeconds(Math.min(60L, 5L * attempts));
        this.lastError = error == null ? "Unknown slot sync error" : error.substring(0, Math.min(error.length(), 1000));
    }

    public UUID getId() {
        return id;
    }

    public UUID getAppointmentId() {
        return appointmentId;
    }

    public UUID getSlotId() {
        return slotId;
    }

    public String getOperation() {
        return operation;
    }

    public String getTargetStatus() {
        return targetStatus;
    }

    public int getAttempts() {
        return attempts;
    }

    public OffsetDateTime getProcessedAt() {
        return processedAt;
    }

    public OffsetDateTime getNextAttemptAt() {
        return nextAttemptAt;
    }

    public String getLastError() {
        return lastError;
    }
}
