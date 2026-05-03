package com.healthcare.notification.web;

import com.healthcare.notification.dto.MarkNotificationsReadRequest;
import com.healthcare.notification.dto.NotificationPreferenceRequest;
import com.healthcare.notification.dto.NotificationPreferenceResponse;
import com.healthcare.notification.dto.NotificationPageResponse;
import com.healthcare.notification.dto.NotificationResponse;
import com.healthcare.notification.security.CurrentUserResolver;
import com.healthcare.notification.service.NotificationPreferenceService;
import com.healthcare.notification.service.NotificationService;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.web.ApiResponseFactory;
import com.healthcare.shared.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final CurrentUserResolver currentUserResolver;
    private final NotificationService notificationService;
    private final NotificationPreferenceService preferenceService;
    private final ApiResponseFactory apiResponseFactory;

    public NotificationController(CurrentUserResolver currentUserResolver,
                                  NotificationService notificationService,
                                  NotificationPreferenceService preferenceService,
                                  ApiResponseFactory apiResponseFactory) {
        this.currentUserResolver = currentUserResolver;
        this.notificationService = notificationService;
        this.preferenceService = preferenceService;
        this.apiResponseFactory = apiResponseFactory;
    }

    @GetMapping
    public ApiResponse<NotificationPageResponse> list(HttpServletRequest request,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        AuthenticatedUser user = currentUserResolver.resolve(request);
        return apiResponseFactory.success("Notifications loaded", notificationService.list(user.userId(), page, size));
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationResponse> markRead(HttpServletRequest request, @PathVariable UUID id) {
        AuthenticatedUser user = currentUserResolver.resolve(request);
        return apiResponseFactory.success("Notification marked read", notificationService.markRead(user.userId(), id));
    }

    @PatchMapping("/mark-read")
    public ApiResponse<NotificationPageResponse> markRead(HttpServletRequest request,
                                                          @Valid @RequestBody MarkNotificationsReadRequest body) {
        AuthenticatedUser user = currentUserResolver.resolve(request);
        return apiResponseFactory.success("Notifications marked read", notificationService.markRead(user.userId(), body.ids()));
    }

    @GetMapping("/preferences")
    public ApiResponse<NotificationPreferenceResponse> getPreferences(HttpServletRequest request) {
        AuthenticatedUser user = currentUserResolver.resolve(request);
        return apiResponseFactory.success("Notification preferences loaded", preferenceService.get(user.userId()));
    }

    @PatchMapping("/preferences")
    public ApiResponse<NotificationPreferenceResponse> updatePreferences(HttpServletRequest request,
                                                                         @RequestBody NotificationPreferenceRequest body) {
        AuthenticatedUser user = currentUserResolver.resolve(request);
        return apiResponseFactory.success("Notification preferences updated", preferenceService.update(user.userId(), body));
    }
}
