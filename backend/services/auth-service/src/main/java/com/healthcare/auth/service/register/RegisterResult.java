package com.healthcare.auth.service.register;

import java.util.UUID;

public record RegisterResult(
        UUID userId,
        String email,
        String role,
        String status
) {
}
