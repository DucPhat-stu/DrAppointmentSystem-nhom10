package com.healthcare.auth.repository;

import com.healthcare.auth.service.token.RefreshTokenReader;
import com.healthcare.auth.service.token.RefreshTokenSession;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaRefreshTokenReader implements RefreshTokenReader {
    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    public JpaRefreshTokenReader(RefreshTokenJpaRepository refreshTokenJpaRepository) {
        this.refreshTokenJpaRepository = refreshTokenJpaRepository;
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
}
