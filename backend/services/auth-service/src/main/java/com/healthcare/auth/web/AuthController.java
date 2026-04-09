package com.healthcare.auth.web;

import com.healthcare.auth.application.LoginCommand;
import com.healthcare.auth.application.LoginResult;
import com.healthcare.auth.application.LoginUseCase;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.web.ApiResponseFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final LoginUseCase loginUseCase;
    private final ApiResponseFactory apiResponseFactory;

    public AuthController(LoginUseCase loginUseCase, ApiResponseFactory apiResponseFactory) {
        this.loginUseCase = loginUseCase;
        this.apiResponseFactory = apiResponseFactory;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResult result = loginUseCase.login(new LoginCommand(request.email(), request.password()));
        LoginResponse response = new LoginResponse(
                result.accessToken(),
                result.refreshToken(),
                result.expiresInSeconds(),
                result.role()
        );

        return apiResponseFactory.success("Login successful", response);
    }
}
