package com.healthcare.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TwoFactorVerifyRequest(
        @NotBlank(message = "Code is required")
        @Pattern(regexp = "\\d{6}", message = "2FA code must be 6 digits")
        String code
) {
}
