package com.healthcare.notification.web;

import com.healthcare.notification.dto.BroadcastNotificationRequest;
import com.healthcare.notification.security.CurrentUserResolver;
import com.healthcare.notification.service.NotificationService;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import com.healthcare.shared.common.web.ApiResponseFactory;
import com.healthcare.shared.security.AuthenticatedUser;
import com.healthcare.shared.security.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/notifications")
public class AdminNotificationController {
    private final CurrentUserResolver currentUserResolver;
    private final NotificationService notificationService;
    private final ApiResponseFactory apiResponseFactory;

    public AdminNotificationController(CurrentUserResolver currentUserResolver,
                                       NotificationService notificationService,
                                       ApiResponseFactory apiResponseFactory) {
        this.currentUserResolver = currentUserResolver;
        this.notificationService = notificationService;
        this.apiResponseFactory = apiResponseFactory;
    }

    @PostMapping("/broadcast")
    public ApiResponse<Map<String, Integer>> broadcast(HttpServletRequest request,
                                                       @Valid @RequestBody BroadcastNotificationRequest body) {
        AuthenticatedUser user = currentUserResolver.resolve(request);
        if (user.role() != Role.ADMIN && user.role() != Role.SUPER_ADMIN) {
            throw new ApiException(ErrorCode.INSUFFICIENT_PERMISSIONS, "Admin role is required");
        }
        int created = notificationService.broadcast(
                body.recipientIds(),
                UUID.randomUUID(),
                body.type() == null || body.type().isBlank() ? "BROADCAST" : body.type(),
                body.title(),
                body.content()
        );
        return apiResponseFactory.success("Broadcast queued", Map.of("created", created));
    }
}
