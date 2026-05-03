package com.healthcare.auth.controller;

import com.healthcare.auth.dto.request.ChangePasswordRequest;
import com.healthcare.auth.dto.request.ForgotPasswordRequest;
import com.healthcare.auth.dto.request.ResetPasswordRequest;
import com.healthcare.auth.service.password.PasswordManagementService;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.web.ApiResponseFactory;
import com.healthcare.shared.security.ForwardedHeaders;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controller for password management: forgot, reset, change.
 * UC-01-07 (Forgot), UC-01-08 (Reset), UC-01-09 (Change).
 */
@RestController
@RequestMapping("/api/v1/auth")
public class PasswordController {

    private final PasswordManagementService passwordManagementService;
    private final ApiResponseFactory apiResponseFactory;

    public PasswordController(PasswordManagementService passwordManagementService,
                              ApiResponseFactory apiResponseFactory) {
        this.passwordManagementService = passwordManagementService;
        this.apiResponseFactory = apiResponseFactory;
    }

    /**
     * POST /api/v1/auth/forgot-password
     * Sends a password reset token (logged to console in MVP mock).
     */
    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordManagementService.forgotPassword(request.email());
        return apiResponseFactory.success("If the email exists, a reset link has been sent", null);
    }

    /**
     * POST /api/v1/auth/reset-password
     * Resets the password using the provided token.
     */
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordManagementService.resetPassword(request.token(), request.newPassword());
        return apiResponseFactory.success("Password has been reset successfully", null);
    }

    /**
     * POST /api/v1/auth/change-password
     * Changes password for the authenticated user (requires current password).
     */
    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(HttpServletRequest httpRequest,
                                            @Valid @RequestBody ChangePasswordRequest request) {
        String rawUserId = (String) httpRequest.getAttribute(ForwardedHeaders.USER_ID);
        if (rawUserId == null) {
            throw new com.healthcare.shared.common.exception.ApiException(
                    com.healthcare.shared.api.ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        UUID userId = UUID.fromString(rawUserId);
        passwordManagementService.changePassword(userId, request.currentPassword(), request.newPassword());
        return apiResponseFactory.success("Password changed successfully", null);
    }
}
