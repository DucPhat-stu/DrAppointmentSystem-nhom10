package com.healthcare.auth.dto.response;

import java.util.UUID;

public record RegisterResponse(
        UUID userId,
        String email,
        String role,
        String status
) {
}
