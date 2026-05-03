package com.healthcare.user.web;

import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.web.ApiResponseFactory;
import com.healthcare.shared.security.ForwardedHeaders;
import com.healthcare.user.dto.UpdateProfileRequest;
import com.healthcare.user.dto.UserProfileResponse;
import com.healthcare.user.service.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserProfileController {
    private final UserProfileService userProfileService;
    private final ApiResponseFactory apiResponseFactory;

    public UserProfileController(UserProfileService userProfileService,
                                  ApiResponseFactory apiResponseFactory) {
        this.userProfileService = userProfileService;
        this.apiResponseFactory = apiResponseFactory;
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getMyProfile(HttpServletRequest request) {
        UUID userId = extractUserId(request);
        UserProfileResponse profile = userProfileService.getProfile(userId);
        return apiResponseFactory.success("Profile loaded", profile);
    }

    @PutMapping("/me")
    public ApiResponse<UserProfileResponse> updateMyProfile(
            HttpServletRequest request,
            @Valid @RequestBody UpdateProfileRequest body) {
        UUID userId = extractUserId(request);
        UserProfileResponse updated = userProfileService.updateProfile(userId, body);
        return apiResponseFactory.success("Profile updated", updated);
    }

    @PostMapping(value = "/me/avatar", consumes = "multipart/form-data")
    public ApiResponse<UserProfileResponse> uploadAvatar(HttpServletRequest request,
                                                         @RequestPart("file") MultipartFile file) {
        UUID userId = extractUserId(request);
        UserProfileResponse updated = userProfileService.updateAvatar(userId, file);
        return apiResponseFactory.success("Avatar uploaded", updated);
    }

    private UUID extractUserId(HttpServletRequest request) {
        String userIdStr = (String) request.getAttribute(ForwardedHeaders.USER_ID);
        return UUID.fromString(userIdStr);
    }
}
