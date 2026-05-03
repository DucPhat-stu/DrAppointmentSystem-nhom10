package com.healthcare.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OtpLoginRequest(
        @NotBlank(message = "Phone is required")
        String phone
) {
}
