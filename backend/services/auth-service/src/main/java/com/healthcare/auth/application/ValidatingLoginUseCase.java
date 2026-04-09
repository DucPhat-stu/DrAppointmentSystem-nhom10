package com.healthcare.auth.application;

import com.healthcare.auth.domain.UserStatus;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ValidatingLoginUseCase implements LoginUseCase {
    private final UserCredentialReader userCredentialReader;
    private final PasswordVerifier passwordVerifier;
    private final LoginTokenIssuer loginTokenIssuer;
    private final RefreshTokenWriter refreshTokenWriter;
    private final UserLoginTracker userLoginTracker;

    public ValidatingLoginUseCase(UserCredentialReader userCredentialReader,
                                  PasswordVerifier passwordVerifier,
                                  LoginTokenIssuer loginTokenIssuer,
                                  RefreshTokenWriter refreshTokenWriter,
                                  UserLoginTracker userLoginTracker) {
        this.userCredentialReader = userCredentialReader;
        this.passwordVerifier = passwordVerifier;
        this.loginTokenIssuer = loginTokenIssuer;
        this.refreshTokenWriter = refreshTokenWriter;
        this.userLoginTracker = userLoginTracker;
    }

    @Override
    @Transactional
    public LoginResult login(LoginCommand command) {
        String normalizedEmail = command.email().trim().toLowerCase();
        UserCredential userCredential = userCredentialReader.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "Invalid email or password"));

        if (userCredential.status() != UserStatus.ACTIVE) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "User account is not active");
        }

        if (!passwordVerifier.matches(command.password(), userCredential.passwordHash())) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid email or password");
        }

        IssuedTokenPair issuedTokenPair = loginTokenIssuer.issue(userCredential);
        refreshTokenWriter.save(new RefreshTokenRecord(
                userCredential.userId(),
                issuedTokenPair.refreshToken(),
                issuedTokenPair.refreshTokenExpiresAt(),
                issuedTokenPair.issuedAt()
        ));
        userLoginTracker.markSuccessfulLogin(userCredential.userId(), issuedTokenPair.issuedAt());

        return new LoginResult(
                issuedTokenPair.accessToken(),
                issuedTokenPair.refreshToken(),
                issuedTokenPair.expiresInSeconds(),
                userCredential.role().name()
        );
    }
}
