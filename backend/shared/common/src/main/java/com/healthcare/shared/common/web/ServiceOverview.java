package com.healthcare.shared.common.web;

public record ServiceOverview(
        String name,
        String description,
        String database,
        boolean rabbitmqEnabled,
        boolean redisEnabled
) {
}

