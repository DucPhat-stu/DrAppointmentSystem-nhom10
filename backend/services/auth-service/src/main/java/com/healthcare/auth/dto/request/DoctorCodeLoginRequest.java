package com.healthcare.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DoctorCodeLoginRequest(
        @NotBlank(message = "Doctor code is required")
        String doctorCode
) {
}
