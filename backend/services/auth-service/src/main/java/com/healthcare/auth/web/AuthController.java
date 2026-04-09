package com.healthcare.auth.web;

import com.healthcare.auth.application.LoginCommand;
import com.healthcare.auth.application.LoginResult;
import com.healthcare.auth.application.LoginUseCase;
import com.healthcare.auth.application.LogoutCommand;
import com.healthcare.auth.application.LogoutUseCase;
import com.healthcare.auth.application.RefreshCommand;
import com.healthcare.auth.application.RefreshResult;
import com.healthcare.auth.application.RefreshUseCase;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.web.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final LoginUseCase loginUseCase;
    private final RefreshUseCase refreshUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ApiResponseFactory apiResponseFactory;

    public AuthController(LoginUseCase loginUseCase,
                          RefreshUseCase refreshUseCase,
                          LogoutUseCase logoutUseCase,
                          ApiResponseFactory apiResponseFactory) {
        this.loginUseCase = loginUseCase;
        this.refreshUseCase = refreshUseCase;
        this.logoutUseCase = logoutUseCase;
        this.apiResponseFactory = apiResponseFactory;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = loginUseCase.login(new LoginCommand(request.email(), request.password()));
        LoginResponse response = new LoginResponse(
                result.accessToken(),
                result.refreshToken(),
                result.expiresInSeconds(),
                result.role(),
                result.permissions()
        );

        return apiResponseFactory.success("Login successful", response);
    }

    @PostMapping("/refresh")
    public ApiResponse<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        RefreshResult result = refreshUseCase.refresh(new RefreshCommand(request.refreshToken()));
        RefreshResponse response = new RefreshResponse(
                result.accessToken(),
                result.expiresInSeconds(),
                result.role(),
                result.permissions()
        );

        return apiResponseFactory.success("Token refreshed successfully", response);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
        logoutUseCase.logout(new LogoutCommand(request.refreshToken()));
        return apiResponseFactory.success("Logout successful", null);
    }
}
