package com.healthcare.auth.controller;

import com.healthcare.auth.dto.request.DoctorCodeLoginRequest;
import com.healthcare.auth.dto.request.OtpLoginRequest;
import com.healthcare.auth.dto.request.OtpVerifyRequest;
import com.healthcare.auth.dto.request.TwoFactorLoginRequest;
import com.healthcare.auth.dto.request.TwoFactorVerifyRequest;
import com.healthcare.auth.dto.response.LoginResponse;
import com.healthcare.auth.dto.response.TwoFactorSetupResponse;
import com.healthcare.auth.service.login.AuthMockLoginService;
import com.healthcare.auth.service.login.LoginResult;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.exception.ApiException;
import com.healthcare.shared.common.web.ApiResponseFactory;
import com.healthcare.shared.security.ForwardedHeaders;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.healthcare.shared.api.ErrorCode.UNAUTHORIZED;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthMockController {
    private static final String MOCK_CODE = "123456";

    private final AuthMockLoginService authMockLoginService;
    private final ApiResponseFactory apiResponseFactory;

    public AuthMockController(AuthMockLoginService authMockLoginService,
                              ApiResponseFactory apiResponseFactory) {
        this.authMockLoginService = authMockLoginService;
        this.apiResponseFactory = apiResponseFactory;
    }

    @PostMapping("/otp/request")
    public ApiResponse<Void> requestOtp(@Valid @RequestBody OtpLoginRequest request) {
        authMockLoginService.requestOtp(request.phone());
        return apiResponseFactory.success("If the phone exists, mock OTP 123456 has been sent", null);
    }

    @PostMapping("/otp/verify")
    public ApiResponse<LoginResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return apiResponseFactory.success("OTP login successful", toResponse(
                authMockLoginService.verifyOtp(request.phone(), request.otp())
        ));
    }

    @PostMapping("/doctor-code/login")
    public ApiResponse<LoginResponse> loginByDoctorCode(@Valid @RequestBody DoctorCodeLoginRequest request) {
        return apiResponseFactory.success("Doctor code login successful", toResponse(
                authMockLoginService.loginByDoctorCode(request.doctorCode())
        ));
    }

    @PostMapping("/2fa/setup")
    public ApiResponse<TwoFactorSetupResponse> setupTwoFactor(HttpServletRequest request) {
        String secret = authMockLoginService.setupTwoFactor(currentUserId(request));
        return apiResponseFactory.success("Mock 2FA setup created", new TwoFactorSetupResponse(secret, MOCK_CODE));
    }

    @PostMapping("/2fa/verify")
    public ApiResponse<Void> verifyTwoFactor(HttpServletRequest request,
                                             @Valid @RequestBody TwoFactorVerifyRequest body) {
        authMockLoginService.verifyTwoFactor(currentUserId(request), body.code());
        return apiResponseFactory.success("Mock 2FA enabled", null);
    }

    @PostMapping("/2fa/login")
    public ApiResponse<LoginResponse> loginWithTwoFactor(@Valid @RequestBody TwoFactorLoginRequest request) {
        return apiResponseFactory.success("2FA login successful", toResponse(
                authMockLoginService.loginWithTwoFactor(request.email(), request.password(), request.code())
        ));
    }

    private UUID currentUserId(HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute(ForwardedHeaders.USER_ID);
        if (rawUserId == null) {
            throw new ApiException(UNAUTHORIZED, "Authentication required");
        }
        return UUID.fromString(rawUserId);
    }

    private LoginResponse toResponse(LoginResult result) {
        return new LoginResponse(
                result.userId(),
                result.accessToken(),
                result.refreshToken(),
                result.expiresInSeconds(),
                result.role(),
                result.permissions()
        );
    }
}
