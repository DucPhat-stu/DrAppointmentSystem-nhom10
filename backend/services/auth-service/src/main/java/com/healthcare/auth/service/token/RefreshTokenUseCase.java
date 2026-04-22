package com.healthcare.auth.service.token;

import com.healthcare.auth.entity.UserStatus;
import com.healthcare.auth.service.login.UserCredential;
import com.healthcare.auth.service.login.UserCredentialReader;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;

@Service
public class RefreshTokenUseCase implements RefreshUseCase {
    private final RefreshTokenReader refreshTokenReader;
    private final RefreshTokenLifecycleManager refreshTokenLifecycleManager;
    private final UserCredentialReader userCredentialReader;
    private final AccessTokenIssuer accessTokenIssuer;
    private final Clock clock;

    public RefreshTokenUseCase(RefreshTokenReader refreshTokenReader,
                               RefreshTokenLifecycleManager refreshTokenLifecycleManager,
                               UserCredentialReader userCredentialReader,
                               AccessTokenIssuer accessTokenIssuer,
                               Clock clock) {
        this.refreshTokenReader = refreshTokenReader;
        this.refreshTokenLifecycleManager = refreshTokenLifecycleManager;
        this.userCredentialReader = userCredentialReader;
        this.accessTokenIssuer = accessTokenIssuer;
        this.clock = clock;
    }

    @Override
    @Transactional
    public RefreshResult refresh(RefreshCommand command) {
        RefreshTokenSession session = refreshTokenReader.findByToken(command.refreshToken())
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "Invalid refresh token"));

        if (session.revoked()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Refresh token has been revoked");
        }

        OffsetDateTime now = OffsetDateTime.now(clock);
        if (session.expiresAt().isBefore(now)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Refresh token has expired");
        }

        UserCredential userCredential = userCredentialReader.findById(session.userId())
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "User not found for refresh token"));

        if (userCredential.status() != UserStatus.ACTIVE) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "User account is not active");
        }

        AccessTokenResult accessTokenResult = accessTokenIssuer.issueAccessToken(userCredential);
        refreshTokenLifecycleManager.markUsed(session.tokenId(), now);

        return new RefreshResult(
                accessTokenResult.accessToken(),
                accessTokenResult.expiresInSeconds(),
                userCredential.role().name(),
                accessTokenResult.permissions()
        );
    }
}
