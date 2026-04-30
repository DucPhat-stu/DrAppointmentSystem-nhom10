package com.healthcare.ai.security;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class FixedWindowRateLimiter {
    private final Clock clock;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public FixedWindowRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public boolean tryAcquire(String key, int limit, Duration duration) {
        if (limit <= 0) {
            return false;
        }

        long now = clock.millis();
        long windowMillis = duration.toMillis();
        AtomicBoolean allowed = new AtomicBoolean(false);

        windows.compute(key, (ignored, current) -> {
            if (current == null || now - current.startedAtMillis() >= windowMillis) {
                allowed.set(true);
                return new Window(now, 1);
            }

            if (current.count() >= limit) {
                return current;
            }

            allowed.set(true);
            return new Window(current.startedAtMillis(), current.count() + 1);
        });

        return allowed.get();
    }

    public long retryAfterSeconds(String key, Duration duration) {
        Window window = windows.get(key);
        if (window == null) {
            return duration.toSeconds();
        }

        long elapsedMillis = Math.max(0, clock.millis() - window.startedAtMillis());
        long remainingMillis = Math.max(0, duration.toMillis() - elapsedMillis);
        return Math.max(1, (remainingMillis + 999) / 1000);
    }

    private record Window(long startedAtMillis, int count) {
    }
}
