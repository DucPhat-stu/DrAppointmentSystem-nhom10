package com.healthcare.appointment.config;

import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class InternalServiceTokenInterceptor implements HandlerInterceptor {
    public static final String HEADER_NAME = "X-Internal-Service-Token";

    private final InternalServiceProperties properties;

    public InternalServiceTokenInterceptor(InternalServiceProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String expected = properties.getServiceToken();
        String provided = request.getHeader(HEADER_NAME);
        if (expected == null || expected.isBlank() || provided == null || provided.isBlank()
                || !MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8))) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid internal service token");
        }
        return true;
    }
}
