package com.healthcare.auth.infrastructure.persistence;

import com.healthcare.auth.application.RefreshTokenLifecycleManager;
import com.healthcare.auth.application.RefreshTokenRecord;
import com.healthcare.auth.application.RefreshTokenReader;
import com.healthcare.auth.application.RefreshTokenSession;
import com.healthcare.auth.application.RefreshTokenWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaRefreshTokenWriter implements RefreshTokenWriter, RefreshTokenReader, RefreshTokenLifecycleManager {
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

    @Override
    public Optional<RefreshTokenSession> findByToken(String token) {
        return refreshTokenJpaRepository.findByToken(token)
                .map(entity -> new RefreshTokenSession(
                        entity.getId(),
                        entity.getUserId(),
                        entity.getToken(),
                        entity.getExpiresAt(),
                        entity.isRevoked()
                ));
    }

    @Override
    @Transactional
    public void markUsed(UUID tokenId, OffsetDateTime usedAt) {
        RefreshTokenEntity entity = refreshTokenJpaRepository.findById(tokenId)
                .orElseThrow();
        entity.setLastUsedAt(usedAt);
        refreshTokenJpaRepository.save(entity);
    }

    @Override
    @Transactional
    public void revoke(UUID tokenId, OffsetDateTime revokedAt) {
        RefreshTokenEntity entity = refreshTokenJpaRepository.findById(tokenId)
                .orElseThrow();
        entity.setRevoked(true);
        entity.setRevokedAt(revokedAt);
        refreshTokenJpaRepository.save(entity);
    }
}
