package com.healthcare.ai.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class FixedWindowRateLimiterTest {
    private final FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(
            Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)
    );

    @Test
    void rejectsRequestsAfterLimitIsReached() {
        assertThat(limiter.tryAcquire("user-1", 2, Duration.ofMinutes(1))).isTrue();
        assertThat(limiter.tryAcquire("user-1", 2, Duration.ofMinutes(1))).isTrue();
        assertThat(limiter.tryAcquire("user-1", 2, Duration.ofMinutes(1))).isFalse();
    }

    @Test
    void rejectsWhenLimitIsDisabledByZero() {
        assertThat(limiter.tryAcquire("user-1", 0, Duration.ofMinutes(1))).isFalse();
    }
}
