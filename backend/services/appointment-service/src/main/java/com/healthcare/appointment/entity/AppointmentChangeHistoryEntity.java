package com.healthcare.appointment.entity;

import com.healthcare.appointment.domain.AppointmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointment_change_history")
public class AppointmentChangeHistoryEntity {
    @Id
    private UUID id;

    @Column(name = "appointment_id", nullable = false)
    private UUID appointmentId;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Column(name = "actor_role", nullable = false, length = 40)
    private String actorRole;

    @Column(name = "change_type", nullable = false, length = 40)
    private String changeType;

    @Column(name = "old_slot_id")
    private UUID oldSlotId;

    @Column(name = "new_slot_id")
    private UUID newSlotId;

    @Column(name = "old_scheduled_start")
    private OffsetDateTime oldScheduledStart;

    @Column(name = "old_scheduled_end")
    private OffsetDateTime oldScheduledEnd;

    @Column(name = "new_scheduled_start")
    private OffsetDateTime newScheduledStart;

    @Column(name = "new_scheduled_end")
    private OffsetDateTime newScheduledEnd;

    @Column(name = "old_status", length = 40)
    private String oldStatus;

    @Column(name = "new_status", length = 40)
    private String newStatus;

    @Column(length = 500)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public static AppointmentChangeHistoryEntity reschedule(UUID actorId,
                                                            AppointmentEntity appointment,
                                                            UUID oldSlotId,
                                                            OffsetDateTime oldScheduledStart,
                                                            OffsetDateTime oldScheduledEnd,
                                                            AppointmentStatus oldStatus,
                                                            String reason) {
        AppointmentChangeHistoryEntity entity = new AppointmentChangeHistoryEntity();
        entity.id = UUID.randomUUID();
        entity.appointmentId = appointment.getId();
        entity.actorId = actorId;
        entity.actorRole = "PATIENT";
        entity.changeType = "RESCHEDULE";
        entity.oldSlotId = oldSlotId;
        entity.newSlotId = appointment.getSlotId();
        entity.oldScheduledStart = oldScheduledStart;
        entity.oldScheduledEnd = oldScheduledEnd;
        entity.newScheduledStart = appointment.getScheduledStart();
        entity.newScheduledEnd = appointment.getScheduledEnd();
        entity.oldStatus = oldStatus == null ? null : oldStatus.name();
        entity.newStatus = appointment.getStatus() == null ? null : appointment.getStatus().name();
        entity.reason = reason;
        entity.createdAt = OffsetDateTime.now();
        return entity;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public UUID getAppointmentId() {
        return appointmentId;
    }

    public UUID getActorId() {
        return actorId;
    }

    public String getActorRole() {
        return actorRole;
    }

    public String getChangeType() {
        return changeType;
    }

    public UUID getOldSlotId() {
        return oldSlotId;
    }

    public UUID getNewSlotId() {
        return newSlotId;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public String getReason() {
        return reason;
    }
}
