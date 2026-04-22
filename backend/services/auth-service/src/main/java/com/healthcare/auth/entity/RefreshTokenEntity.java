package com.healthcare.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(nullable = false)
    private boolean revoked;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "last_used_at")
    private OffsetDateTime lastUsedAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    @Column(name = "device_info")
    private String deviceInfo;

    public void setId(UUID id) {
        this.id = id;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setLastUsedAt(OffsetDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public void setRevokedAt(OffsetDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }
}
