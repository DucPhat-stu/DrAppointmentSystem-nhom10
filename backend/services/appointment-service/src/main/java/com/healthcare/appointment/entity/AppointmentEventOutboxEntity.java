package com.healthcare.appointment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointment_event_outbox")
public class AppointmentEventOutboxEntity {
    @Id
    private UUID id;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "appointment_id", nullable = false)
    private UUID appointmentId;

    @Column(name = "doctor_id", nullable = false)
    private UUID doctorId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(nullable = false)
    private String payload;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "last_attempt_at")
    private OffsetDateTime lastAttemptAt;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    public static AppointmentEventOutboxEntity from(String eventName, AppointmentEntity appointment) {
        AppointmentEventOutboxEntity entity = new AppointmentEventOutboxEntity();
        entity.id = UUID.randomUUID();
        entity.eventName = eventName;
        entity.appointmentId = appointment.getId();
        entity.doctorId = appointment.getDoctorId();
        entity.patientId = appointment.getPatientId();
        entity.payload = """
                {"eventId":"%s","event":"%s","appointmentId":"%s","doctorId":"%s","patientId":"%s"}""".formatted(
                entity.id,
                eventName,
                appointment.getId(),
                appointment.getDoctorId(),
                appointment.getPatientId()
        );
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

    public void markPublished(OffsetDateTime publishedAt) {
        this.publishedAt = publishedAt;
        this.lastAttemptAt = publishedAt;
        this.lastError = null;
    }

    public void markFailed(String error, OffsetDateTime attemptedAt) {
        this.attempts++;
        this.lastAttemptAt = attemptedAt;
        this.lastError = error == null ? "Unknown publish error" : error.substring(0, Math.min(error.length(), 1000));
    }

    public String getEventName() {
        return eventName;
    }

    public String getPayload() {
        return payload;
    }
}
