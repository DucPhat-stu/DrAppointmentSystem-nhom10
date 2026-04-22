package com.healthcare.auth.repository;

import com.healthcare.auth.entity.RefreshTokenEntity;
import com.healthcare.auth.service.token.RefreshTokenLifecycleManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class JpaRefreshTokenLifecycleManager implements RefreshTokenLifecycleManager {
    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    public JpaRefreshTokenLifecycleManager(RefreshTokenJpaRepository refreshTokenJpaRepository) {
        this.refreshTokenJpaRepository = refreshTokenJpaRepository;
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
