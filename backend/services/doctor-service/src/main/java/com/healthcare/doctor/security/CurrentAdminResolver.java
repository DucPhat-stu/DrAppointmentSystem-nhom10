package com.healthcare.doctor.security;

import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import com.healthcare.shared.security.AuthenticatedUser;
import com.healthcare.shared.security.ForwardedHeaders;
import com.healthcare.shared.security.Role;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentAdminResolver {
    public AuthenticatedUser resolve(HttpServletRequest request) {
        UUID userId = parseUserId(readForwardedValue(request, ForwardedHeaders.USER_ID));
        Role role = parseRole(readForwardedValue(request, ForwardedHeaders.USER_ROLE));
        if (role != Role.ADMIN && role != Role.SUPER_ADMIN) {
            throw new ApiException(ErrorCode.INSUFFICIENT_PERMISSIONS, "Admin role is required");
        }
        return new AuthenticatedUser(userId, role);
    }

    private String readForwardedValue(HttpServletRequest request, String name) {
        Object attribute = request.getAttribute(name);
        if (attribute instanceof String value && !value.isBlank()) {
            return value;
        }
        throw new ApiException(ErrorCode.UNAUTHORIZED, "Missing authenticated user context");
    }

    private UUID parseUserId(String rawUserId) {
        try {
            return UUID.fromString(rawUserId);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid authenticated user id");
        }
    }

    private Role parseRole(String rawRole) {
        try {
            return Role.valueOf(rawRole);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid authenticated user role");
        }
    }
}
