package com.healthcare.auth.service.register;

import com.healthcare.auth.entity.UserStatus;
import com.healthcare.auth.service.login.UserCredentialReader;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import com.healthcare.shared.security.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
public class SelfRegistrationUseCase implements RegisterUseCase {
    private final UserCredentialReader userCredentialReader;
    private final PasswordHasher passwordHasher;
    private final UserRegistrationWriter userRegistrationWriter;
    private final Clock clock;

    public SelfRegistrationUseCase(UserCredentialReader userCredentialReader,
                                   PasswordHasher passwordHasher,
                                   UserRegistrationWriter userRegistrationWriter,
                                   Clock clock) {
        this.userCredentialReader = userCredentialReader;
        this.passwordHasher = passwordHasher;
        this.userRegistrationWriter = userRegistrationWriter;
        this.clock = clock;
    }

    @Override
    @Transactional
    public RegisterResult register(RegisterCommand command) {
        String normalizedEmail = normalizeEmail(command.email());
        if (userCredentialReader.findByEmail(normalizedEmail).isPresent()) {
            throw new ApiException(ErrorCode.CONFLICT, "Email already exists");
        }

        OffsetDateTime createdAt = OffsetDateTime.now(clock);
        UUID userId = UUID.randomUUID();

        // MVP local keeps self-registration immediately usable until OTP verification is wired in.
        NewUserAccount userAccount = new NewUserAccount(
                userId,
                normalizedEmail,
                passwordHasher.hash(command.password()),
                Role.PATIENT,
                UserStatus.ACTIVE,
                command.name().trim(),
                normalizePhone(command.phone()),
                createdAt
        );

        userRegistrationWriter.save(userAccount);

        return new RegisterResult(
                userId,
                normalizedEmail,
                Role.PATIENT.name(),
                userAccount.status().name()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }

        String normalizedPhone = phone.trim();
        return normalizedPhone.isEmpty() ? null : normalizedPhone;
    }
}
