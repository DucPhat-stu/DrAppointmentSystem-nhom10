package com.healthcare.shared.api;

import java.time.Instant;

public record ApiMeta(String requestId, Instant timestamp) {
}

