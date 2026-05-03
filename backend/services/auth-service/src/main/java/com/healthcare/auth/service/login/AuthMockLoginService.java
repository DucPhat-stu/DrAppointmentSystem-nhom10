package com.healthcare.auth.service.login;

import com.healthcare.auth.entity.UserAccountEntity;
import com.healthcare.auth.entity.UserStatus;
import com.healthcare.auth.repository.UserAccountJpaRepository;
import com.healthcare.auth.service.token.RefreshTokenRecord;
import com.healthcare.auth.service.token.RefreshTokenWriter;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import com.healthcare.shared.security.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AuthMockLoginService {
    private static final Logger log = LoggerFactory.getLogger(AuthMockLoginService.class);
    private static final String MOCK_CODE = "123456";

    private final UserAccountJpaRepository userRepository;
    private final PasswordVerifier passwordVerifier;
    private final LoginTokenIssuer loginTokenIssuer;
    private final RefreshTokenWriter refreshTokenWriter;
    private final UserLoginTracker userLoginTracker;

    public AuthMockLoginService(UserAccountJpaRepository userRepository,
                                PasswordVerifier passwordVerifier,
                                LoginTokenIssuer loginTokenIssuer,
                                RefreshTokenWriter refreshTokenWriter,
                                UserLoginTracker userLoginTracker) {
        this.userRepository = userRepository;
        this.passwordVerifier = passwordVerifier;
        this.loginTokenIssuer = loginTokenIssuer;
        this.refreshTokenWriter = refreshTokenWriter;
        this.userLoginTracker = userLoginTracker;
    }

    @Transactional
    public void requestOtp(String phone) {
        userRepository.findByPhone(phone.trim()).ifPresent(user -> {
            user.setPhoneOtp(MOCK_CODE);
            user.setPhoneOtpExpiresAt(OffsetDateTime.now().plusMinutes(5));
            user.setUpdatedAt(OffsetDateTime.now());
            userRepository.save(user);
            log.info("[MOCK SMS] OTP for {} is {}", phone, MOCK_CODE);
        });
    }

    @Transactional
    public LoginResult verifyOtp(String phone, String otp) {
        UserAccountEntity user = userRepository.findByPhone(phone.trim())
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "Invalid phone or OTP"));
        if (!MOCK_CODE.equals(otp) || user.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid phone or OTP");
        }
        if (user.getPhoneOtpExpiresAt() != null && user.getPhoneOtpExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "OTP has expired");
        }
        user.setPhoneOtp(null);
        user.setPhoneOtpExpiresAt(null);
        userRepository.save(user);
        return issueLogin(user);
    }

    @Transactional
    public LoginResult loginByDoctorCode(String doctorCode) {
        UserAccountEntity user = userRepository.findByDoctorCodeIgnoreCase(doctorCode.trim())
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "Invalid doctor code"));
        if (user.getRole() != Role.DOCTOR || user.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid doctor code");
        }
        return issueLogin(user);
    }

    @Transactional
    public String setupTwoFactor(UUID userId) {
        UserAccountEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));
        String secret = "MOCK-" + UUID.randomUUID();
        user.setTwoFactorSecret(secret);
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);
        log.info("[MOCK 2FA] Setup secret for {} is {}, verification code {}", user.getEmail(), secret, MOCK_CODE);
        return secret;
    }

    @Transactional
    public void verifyTwoFactor(UUID userId, String code) {
        if (!MOCK_CODE.equals(code)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid 2FA code");
        }
        UserAccountEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));
        if (user.getTwoFactorSecret() == null) {
            user.setTwoFactorSecret("MOCK-" + UUID.randomUUID());
        }
        user.setTwoFactorEnabled(true);
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public LoginResult loginWithTwoFactor(String email, String password, String code) {
        UserAccountEntity user = userRepository.findByEmailIgnoreCase(email.trim().toLowerCase())
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "Invalid email, password, or 2FA code"));
        if (user.getStatus() != UserStatus.ACTIVE
                || !passwordVerifier.matches(password, user.getPasswordHash())
                || !MOCK_CODE.equals(code)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid email, password, or 2FA code");
        }
        return issueLogin(user);
    }

    private LoginResult issueLogin(UserAccountEntity user) {
        UserCredential credential = new UserCredential(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole(),
                user.getStatus()
        );
        IssuedTokenPair issuedTokenPair = loginTokenIssuer.issue(credential);
        refreshTokenWriter.save(new RefreshTokenRecord(
                user.getId(),
                issuedTokenPair.refreshToken(),
                issuedTokenPair.refreshTokenExpiresAt(),
                issuedTokenPair.issuedAt()
        ));
        userLoginTracker.markSuccessfulLogin(user.getId(), issuedTokenPair.issuedAt());
        return new LoginResult(
                user.getId(),
                issuedTokenPair.accessToken(),
                issuedTokenPair.refreshToken(),
                issuedTokenPair.expiresInSeconds(),
                user.getRole().name(),
                issuedTokenPair.permissions()
        );
    }
}
