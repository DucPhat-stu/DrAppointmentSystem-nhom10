package com.healthcare.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "admin_audit_log")
public class AdminAuditLogEntity {

    @Id
    private UUID id;

    @Column(name = "admin_id", nullable = false)
    private UUID adminId;

    @Column(nullable = false, length = 64)
    private String action;

    @Column(name = "target_type", nullable = false, length = 64)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public static AdminAuditLogEntity of(UUID adminId,
                                         String action,
                                         String targetType,
                                         UUID targetId,
                                         String detail) {
        AdminAuditLogEntity entity = new AdminAuditLogEntity();
        entity.id = UUID.randomUUID();
        entity.adminId = adminId;
        entity.action = action;
        entity.targetType = targetType;
        entity.targetId = targetId;
        entity.detail = detail;
        entity.createdAt = OffsetDateTime.now();
        return entity;
    }

    public UUID getId() { return id; }
    public UUID getAdminId() { return adminId; }
    public String getAction() { return action; }
    public String getTargetType() { return targetType; }
    public UUID getTargetId() { return targetId; }
    public String getDetail() { return detail; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
