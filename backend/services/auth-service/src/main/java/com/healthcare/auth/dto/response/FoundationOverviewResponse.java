package com.healthcare.auth.dto.response;

public record FoundationOverviewResponse(
        String name,
        String description,
        String database,
        boolean rabbitmqEnabled,
        boolean redisEnabled
) {
}
