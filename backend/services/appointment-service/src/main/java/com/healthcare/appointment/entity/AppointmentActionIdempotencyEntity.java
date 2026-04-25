package com.healthcare.appointment.entity;

import com.healthcare.appointment.domain.AppointmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointment_action_idempotency")
public class AppointmentActionIdempotencyEntity {
    @Id
    private UUID id;

    @Column(name = "appointment_id", nullable = false)
    private UUID appointmentId;

    @Column(name = "doctor_id", nullable = false)
    private UUID doctorId;

    @Column(name = "action_name", nullable = false)
    private String actionName;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "resulting_status", nullable = false)
    private AppointmentStatus resultingStatus;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public static AppointmentActionIdempotencyEntity create(UUID appointmentId,
                                                            UUID doctorId,
                                                            String actionName,
                                                            String idempotencyKey,
                                                            AppointmentStatus resultingStatus) {
        AppointmentActionIdempotencyEntity entity = new AppointmentActionIdempotencyEntity();
        entity.id = UUID.randomUUID();
        entity.appointmentId = appointmentId;
        entity.doctorId = doctorId;
        entity.actionName = actionName;
        entity.idempotencyKey = idempotencyKey;
        entity.resultingStatus = resultingStatus;
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

    public UUID getId() {
        return id;
    }
}
