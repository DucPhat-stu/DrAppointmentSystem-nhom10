package com.healthcare.auth.dto.response;

public record TwoFactorSetupResponse(
        String secret,
        String mockCode
) {
}
