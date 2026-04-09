package com.healthcare.auth.web;

import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.config.AppServiceProperties;
import com.healthcare.shared.common.web.ApiResponseFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/foundation")
public class FoundationController {
    private final AppServiceProperties properties;
    private final ApiResponseFactory apiResponseFactory;

    public FoundationController(AppServiceProperties properties, ApiResponseFactory apiResponseFactory) {
        this.properties = properties;
        this.apiResponseFactory = apiResponseFactory;
    }

    @GetMapping("/ping")
    public ApiResponse<FoundationOverview> ping() {
        FoundationOverview overview = new FoundationOverview(
                properties.getName(),
                properties.getDescription(),
                properties.getDatabase(),
                properties.isRabbitmqEnabled(),
                properties.isRedisEnabled()
        );

        return apiResponseFactory.success("Foundation skeleton is ready", overview);
    }

    public record FoundationOverview(
            String name,
            String description,
            String database,
            boolean rabbitmqEnabled,
            boolean redisEnabled
    ) {
    }
}

