package com.healthcare.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must be at most 255 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        String email,

        @Pattern(
                regexp = "^$|^[0-9+\\-\\s]{9,20}$",
                message = "Phone number must contain 9 to 20 digits or separators"
        )
        String phone,

        @NotBlank(message = "Password is required")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,72}$",
                message = "Password must be 8-72 characters and include upper, lower, digit, and special character"
        )
        String password
) {
}
