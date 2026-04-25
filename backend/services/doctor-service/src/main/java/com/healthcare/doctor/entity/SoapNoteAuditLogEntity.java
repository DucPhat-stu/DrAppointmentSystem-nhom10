package com.healthcare.doctor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "soap_note_audit_logs")
public class SoapNoteAuditLogEntity {
    @Id
    private UUID id;

    @Column(name = "soap_note_id", nullable = false)
    private UUID soapNoteId;

    @Column(name = "appointment_id", nullable = false)
    private UUID appointmentId;

    @Column(name = "doctor_id", nullable = false)
    private UUID doctorId;

    @Column(name = "action_name", nullable = false)
    private String actionName;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public static SoapNoteAuditLogEntity create(UUID soapNoteId, UUID appointmentId, UUID doctorId, String actionName) {
        SoapNoteAuditLogEntity entity = new SoapNoteAuditLogEntity();
        entity.id = UUID.randomUUID();
        entity.soapNoteId = soapNoteId;
        entity.appointmentId = appointmentId;
        entity.doctorId = doctorId;
        entity.actionName = actionName;
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
}
