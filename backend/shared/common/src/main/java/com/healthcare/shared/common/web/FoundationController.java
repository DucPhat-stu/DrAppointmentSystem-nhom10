package com.healthcare.shared.common.web;

import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.config.AppServiceProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/foundation")
public class FoundationController {
    private final AppServiceProperties properties;

    public FoundationController(AppServiceProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/ping")
    public ApiResponse<ServiceOverview> ping() {
        ServiceOverview overview = new ServiceOverview(
                properties.getName(),
                properties.getDescription(),
                properties.getDatabase(),
                properties.isRabbitmqEnabled(),
                properties.isRedisEnabled()
        );

        return ApiResponseFactory.success("Foundation skeleton is ready", overview);
    }
}
