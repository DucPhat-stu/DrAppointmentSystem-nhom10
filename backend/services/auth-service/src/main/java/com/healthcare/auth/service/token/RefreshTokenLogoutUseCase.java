package com.healthcare.auth.service.token;

import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;

@Service
public class RefreshTokenLogoutUseCase implements LogoutUseCase {
    private final RefreshTokenReader refreshTokenReader;
    private final RefreshTokenLifecycleManager refreshTokenLifecycleManager;
    private final Clock clock;

    public RefreshTokenLogoutUseCase(RefreshTokenReader refreshTokenReader,
                                     RefreshTokenLifecycleManager refreshTokenLifecycleManager,
                                     Clock clock) {
        this.refreshTokenReader = refreshTokenReader;
        this.refreshTokenLifecycleManager = refreshTokenLifecycleManager;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void logout(LogoutCommand command) {
        RefreshTokenSession session = refreshTokenReader.findByToken(command.refreshToken())
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "Invalid refresh token"));

        if (session.revoked()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Refresh token has already been revoked");
        }

        refreshTokenLifecycleManager.revoke(session.tokenId(), OffsetDateTime.now(clock));
    }
}
