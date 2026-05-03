package com.healthcare.auth.service.password;

import com.healthcare.auth.entity.UserAccountEntity;
import com.healthcare.auth.repository.UserAccountJpaRepository;
import com.healthcare.auth.service.login.PasswordVerifier;
import com.healthcare.auth.service.register.PasswordHasher;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Handles forgot password, reset password, and change password flows.
 * MVP mock: password reset token is logged to console instead of sent via email.
 */
@Service
public class PasswordManagementService {

    private static final Logger log = LoggerFactory.getLogger(PasswordManagementService.class);
    private static final int RESET_TOKEN_EXPIRY_MINUTES = 30;

    private final UserAccountJpaRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final PasswordVerifier passwordVerifier;

    public PasswordManagementService(UserAccountJpaRepository userRepository,
                                     PasswordHasher passwordHasher,
                                     PasswordVerifier passwordVerifier) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.passwordVerifier = passwordVerifier;
    }

    /**
     * UC-01-07: Forgot password — generates a reset token and logs it (mock email).
     * Always returns success even if email not found (prevents enumeration).
     */
    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmailIgnoreCase(email.trim()).ifPresent(user -> {
            String resetToken = UUID.randomUUID().toString();
            user.setPasswordResetToken(resetToken);
            user.setPasswordResetExpiresAt(OffsetDateTime.now().plusMinutes(RESET_TOKEN_EXPIRY_MINUTES));
            user.setUpdatedAt(OffsetDateTime.now());
            userRepository.save(user);

            // MVP mock: log token to console instead of sending email
            log.info("[MOCK EMAIL] Password reset link for {}: /reset-password?token={}", user.getEmail(), resetToken);
        });
    }

    /**
     * UC-01-08: Reset password — validates token and sets new password.
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        UserAccountEntity user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new ApiException(ErrorCode.VALIDATION_ERROR, "Invalid or expired reset token"));

        if (user.getPasswordResetExpiresAt() == null || user.getPasswordResetExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Reset token has expired");
        }

        user.setPasswordHash(passwordHasher.hash(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);

        log.info("Password reset successful for user: {}", user.getEmail());
    }

    /**
     * UC-01-09: Change password — requires current password verification.
     */
    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        UserAccountEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        if (!passwordVerifier.matches(currentPassword, user.getPasswordHash())) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Current password is incorrect");
        }

        user.setPasswordHash(passwordHasher.hash(newPassword));
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);

        log.info("Password changed for user: {}", user.getEmail());
    }
}
