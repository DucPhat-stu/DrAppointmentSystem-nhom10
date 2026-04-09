package com.healthcare.auth.infrastructure.persistence;

import com.healthcare.auth.application.RefreshTokenRecord;
import com.healthcare.auth.application.RefreshTokenWriter;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JpaRefreshTokenWriter implements RefreshTokenWriter {
    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    public JpaRefreshTokenWriter(RefreshTokenJpaRepository refreshTokenJpaRepository) {
        this.refreshTokenJpaRepository = refreshTokenJpaRepository;
    }

    @Override
    public void save(RefreshTokenRecord refreshTokenRecord) {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(refreshTokenRecord.userId());
        entity.setToken(refreshTokenRecord.token());
        entity.setExpiresAt(refreshTokenRecord.expiresAt());
        entity.setRevoked(false);
        entity.setCreatedAt(refreshTokenRecord.createdAt());
        entity.setLastUsedAt(null);
        entity.setRevokedAt(null);
        entity.setDeviceInfo("postman-or-client");
        refreshTokenJpaRepository.save(entity);
    }
}

