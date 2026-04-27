package com.healthcare.user.web;

import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.web.ApiResponseFactory;
import com.healthcare.user.dto.UserProfileResponse;
import com.healthcare.user.service.UserProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/users/profiles")
public class InternalUserProfileController {
    private final UserProfileService userProfileService;
    private final ApiResponseFactory apiResponseFactory;

    public InternalUserProfileController(UserProfileService userProfileService,
                                         ApiResponseFactory apiResponseFactory) {
        this.userProfileService = userProfileService;
        this.apiResponseFactory = apiResponseFactory;
    }

    @GetMapping
    public ApiResponse<List<UserProfileResponse>> list(@RequestParam List<UUID> userIds) {
        return apiResponseFactory.success(
                "User profiles loaded",
                userProfileService.getProfiles(userIds)
        );
    }
}
