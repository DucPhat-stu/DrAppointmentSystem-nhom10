package com.healthcare.appointment.security;

import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import com.healthcare.shared.security.AuthenticatedUser;
import com.healthcare.shared.security.ForwardedHeaders;
import com.healthcare.shared.security.Role;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves the authenticated admin from request attributes set by JwtAuthenticationFilter.
 * Throws 403 if the caller is not ADMIN or SUPER_ADMIN.
 */
@Component
public class CurrentAdminResolver {

    public AuthenticatedUser resolve(HttpServletRequest request) {
        UUID userId = parseUserId(readAttribute(request, ForwardedHeaders.USER_ID));
        Role role = parseRole(readAttribute(request, ForwardedHeaders.USER_ROLE));
        if (role != Role.ADMIN && role != Role.SUPER_ADMIN) {
            throw new ApiException(ErrorCode.INSUFFICIENT_PERMISSIONS, "Admin role is required");
        }
        return new AuthenticatedUser(userId, role);
    }

    private String readAttribute(HttpServletRequest request, String name) {
        Object attr = request.getAttribute(name);
        if (attr instanceof String value && !value.isBlank()) {
            return value;
        }
        throw new ApiException(ErrorCode.UNAUTHORIZED, "Missing authenticated user context");
    }

    private UUID parseUserId(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid authenticated user id");
        }
    }

    private Role parseRole(String raw) {
        try {
            return Role.valueOf(raw);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid authenticated user role");
        }
    }
}
