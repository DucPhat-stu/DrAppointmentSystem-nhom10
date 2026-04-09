package com.healthcare.shared.common.web;

import com.healthcare.shared.api.ApiMeta;

import java.time.Clock;
import java.time.Instant;

public class ThreadLocalRequestMetadataContext implements RequestMetadataContext {
    private final ThreadLocal<String> requestIdHolder = new ThreadLocal<>();
    private final Clock clock;

    public ThreadLocalRequestMetadataContext(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void open(String requestId) {
        requestIdHolder.set(requestId);
    }

    @Override
    public void clear() {
        requestIdHolder.remove();
    }

    @Override
    public ApiMeta current() {
        return new ApiMeta(requestIdHolder.get(), Instant.now(clock));
    }
}

