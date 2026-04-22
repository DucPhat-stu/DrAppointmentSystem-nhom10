package com.healthcare.auth.controller;

import com.healthcare.auth.dto.request.LoginRequest;
import com.healthcare.auth.dto.request.LogoutRequest;
import com.healthcare.auth.dto.request.RefreshRequest;
import com.healthcare.auth.dto.request.RegisterRequest;
import com.healthcare.auth.dto.response.LoginResponse;
import com.healthcare.auth.dto.response.RefreshResponse;
import com.healthcare.auth.dto.response.RegisterResponse;
import com.healthcare.auth.service.login.LoginCommand;
import com.healthcare.auth.service.login.LoginResult;
import com.healthcare.auth.service.login.LoginUseCase;
import com.healthcare.auth.service.register.RegisterCommand;
import com.healthcare.auth.service.register.RegisterResult;
import com.healthcare.auth.service.register.RegisterUseCase;
import com.healthcare.auth.service.token.LogoutCommand;
import com.healthcare.auth.service.token.LogoutUseCase;
import com.healthcare.auth.service.token.RefreshCommand;
import com.healthcare.auth.service.token.RefreshResult;
import com.healthcare.auth.service.token.RefreshUseCase;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.web.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshUseCase refreshUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ApiResponseFactory apiResponseFactory;

    public AuthController(RegisterUseCase registerUseCase,
                          LoginUseCase loginUseCase,
                          RefreshUseCase refreshUseCase,
                          LogoutUseCase logoutUseCase,
                          ApiResponseFactory apiResponseFactory) {
        this.registerUseCase = registerUseCase;
        this.loginUseCase = loginUseCase;
        this.refreshUseCase = refreshUseCase;
        this.logoutUseCase = logoutUseCase;
        this.apiResponseFactory = apiResponseFactory;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResult result = registerUseCase.register(new RegisterCommand(
                request.name(),
                request.email(),
                request.phone(),
                request.password()
        ));
        RegisterResponse response = new RegisterResponse(
                result.userId(),
                result.email(),
                result.role(),
                result.status()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(apiResponseFactory.success("Registration successful", response));
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
