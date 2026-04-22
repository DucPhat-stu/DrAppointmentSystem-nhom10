package com.healthcare.auth.service.register;

import com.healthcare.auth.entity.UserStatus;
import com.healthcare.auth.service.login.UserCredential;
import com.healthcare.auth.service.login.UserCredentialReader;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import com.healthcare.shared.security.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SelfRegistrationUseCaseTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-04-22T10:15:30Z"), ZoneOffset.UTC);

    @Mock
    private UserCredentialReader userCredentialReader;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private UserRegistrationWriter userRegistrationWriter;

    private SelfRegistrationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SelfRegistrationUseCase(
                userCredentialReader,
                passwordHasher,
                userRegistrationWriter,
                FIXED_CLOCK
        );
    }

    @Test
    void registerCreatesActivePatientAccountWithNormalizedIdentity() {
        when(userCredentialReader.findByEmail("patient01@healthcare.local")).thenReturn(Optional.empty());
        when(passwordHasher.hash("Patient@123")).thenReturn("$2a$12$hashed");

        RegisterResult result = useCase.register(new RegisterCommand(
                "  Patient One  ",
                "  Patient01@Healthcare.local  ",
                "  +84 901 234 567  ",
                "Patient@123"
        ));

        ArgumentCaptor<NewUserAccount> accountCaptor = ArgumentCaptor.forClass(NewUserAccount.class);
        verify(userRegistrationWriter).save(accountCaptor.capture());

        NewUserAccount savedAccount = accountCaptor.getValue();
        assertThat(savedAccount.email()).isEqualTo("patient01@healthcare.local");
        assertThat(savedAccount.fullName()).isEqualTo("Patient One");
        assertThat(savedAccount.phone()).isEqualTo("+84 901 234 567");
        assertThat(savedAccount.passwordHash()).isEqualTo("$2a$12$hashed");
        assertThat(savedAccount.role()).isEqualTo(Role.PATIENT);
        assertThat(savedAccount.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(savedAccount.createdAt()).isEqualTo(OffsetDateTime.now(FIXED_CLOCK));
        assertThat(result.userId()).isEqualTo(savedAccount.userId());
        assertThat(result.email()).isEqualTo("patient01@healthcare.local");
        assertThat(result.role()).isEqualTo("PATIENT");
        assertThat(result.status()).isEqualTo("ACTIVE");
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userCredentialReader.findByEmail("patient01@healthcare.local")).thenReturn(Optional.of(
                new UserCredential(
                        UUID.randomUUID(),
                        "patient01@healthcare.local",
                        "$2a$12$existing",
                        Role.PATIENT,
                        UserStatus.ACTIVE
                )
        ));

        assertThatThrownBy(() -> useCase.register(new RegisterCommand(
                "Patient One",
                "patient01@healthcare.local",
                null,
                "Patient@123"
        )))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).getErrorCode())
                .isEqualTo(ErrorCode.CONFLICT);
    }
}
