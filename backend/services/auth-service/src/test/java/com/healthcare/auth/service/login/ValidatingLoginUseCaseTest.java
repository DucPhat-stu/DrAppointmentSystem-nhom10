package com.healthcare.auth.service.login;

import com.healthcare.auth.entity.UserStatus;
import com.healthcare.auth.service.token.RefreshTokenRecord;
import com.healthcare.auth.service.token.RefreshTokenWriter;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import com.healthcare.shared.security.Permission;
import com.healthcare.shared.security.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidatingLoginUseCaseTest {
    @Mock
    private UserCredentialReader userCredentialReader;

    @Mock
    private PasswordVerifier passwordVerifier;

    @Mock
    private LoginTokenIssuer loginTokenIssuer;

    @Mock
    private RefreshTokenWriter refreshTokenWriter;

    @Mock
    private UserLoginTracker userLoginTracker;

    @InjectMocks
    private ValidatingLoginUseCase useCase;

    @Test
    void loginTracksFailedAttemptWhenPasswordDoesNotMatch() {
        UUID userId = UUID.randomUUID();
        UserCredential userCredential = new UserCredential(
                userId,
                "patient01@healthcare.local",
                "$2a$12$hash",
                Role.PATIENT,
                UserStatus.ACTIVE
        );

        when(userCredentialReader.findByEmail("patient01@healthcare.local")).thenReturn(Optional.of(userCredential));
        when(passwordVerifier.matches("Wrong@123", "$2a$12$hash")).thenReturn(false);

        assertThatThrownBy(() -> useCase.login(new LoginCommand("patient01@healthcare.local", "Wrong@123", null)))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);

        verify(userLoginTracker).markFailedLoginAttempt(userId);
        verify(refreshTokenWriter, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void loginIssuesTokensAndResetsFailedCounterOnSuccess() {
        UUID userId = UUID.randomUUID();
        OffsetDateTime issuedAt = OffsetDateTime.parse("2026-04-22T10:30:00Z");
        UserCredential userCredential = new UserCredential(
                userId,
                "patient01@healthcare.local",
                "$2a$12$hash",
                Role.PATIENT,
                UserStatus.ACTIVE
        );
        IssuedTokenPair tokenPair = new IssuedTokenPair(
                "access-token",
                "refresh-token",
                900,
                issuedAt,
                issuedAt.plusDays(7),
                EnumSet.of(Permission.AUTH_REFRESH, Permission.AUTH_LOGOUT)
        );

        when(userCredentialReader.findByEmail("patient01@healthcare.local")).thenReturn(Optional.of(userCredential));
        when(passwordVerifier.matches("Patient@123", "$2a$12$hash")).thenReturn(true);
        when(loginTokenIssuer.issue(userCredential)).thenReturn(tokenPair);

        LoginResult result = useCase.login(new LoginCommand("patient01@healthcare.local", "Patient@123", Role.PATIENT));

        ArgumentCaptor<RefreshTokenRecord> tokenCaptor = ArgumentCaptor.forClass(RefreshTokenRecord.class);
        verify(refreshTokenWriter).save(tokenCaptor.capture());
        verify(userLoginTracker).markSuccessfulLogin(userId, issuedAt);
        verify(userLoginTracker, never()).markFailedLoginAttempt(userId);

        RefreshTokenRecord savedToken = tokenCaptor.getValue();
        assertThat(savedToken.userId()).isEqualTo(userId);
        assertThat(savedToken.token()).isEqualTo("refresh-token");
        assertThat(savedToken.createdAt()).isEqualTo(issuedAt);
        assertThat(savedToken.expiresAt()).isEqualTo(issuedAt.plusDays(7));
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.expiresInSeconds()).isEqualTo(900);
        assertThat(result.role()).isEqualTo("PATIENT");
        assertThat(result.permissions()).containsExactlyInAnyOrder(Permission.AUTH_REFRESH, Permission.AUTH_LOGOUT);
    }

    @Test
    void loginRejectsCredentialsWhenRequestedActorDoesNotMatch() {
        UUID userId = UUID.randomUUID();
        UserCredential userCredential = new UserCredential(
                userId,
                "patient01@healthcare.local",
                "$2a$12$hash",
                Role.PATIENT,
                UserStatus.ACTIVE
        );

        when(userCredentialReader.findByEmail("patient01@healthcare.local")).thenReturn(Optional.of(userCredential));
        when(passwordVerifier.matches("Patient@123", "$2a$12$hash")).thenReturn(true);

        assertThatThrownBy(() -> useCase.login(new LoginCommand(
                "patient01@healthcare.local",
                "Patient@123",
                Role.DOCTOR
        )))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);

        verify(loginTokenIssuer, never()).issue(userCredential);
        verify(refreshTokenWriter, never()).save(org.mockito.ArgumentMatchers.any());
        verify(userLoginTracker, never()).markSuccessfulLogin(
                org.mockito.ArgumentMatchers.eq(userId),
                org.mockito.ArgumentMatchers.any()
        );
    }
}
