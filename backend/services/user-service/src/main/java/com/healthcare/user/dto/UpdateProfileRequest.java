package com.healthcare.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProfileRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 255, message = "Full name must not exceed 255 characters")
        String fullName,

        @Pattern(regexp = "^[0-9]{10,11}$", message = "Phone number must be 10-11 digits")
        String phone,

        @Size(max = 500, message = "Address must not exceed 500 characters")
        String address,

        LocalDate dateOfBirth,

        @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER")
        String gender,

        @Size(max = 255, message = "Emergency contact must not exceed 255 characters")
        String emergencyContact,

        @Size(max = 120, message = "Specialty must not exceed 120 characters")
        String specialty,

        @Size(max = 120, message = "Department must not exceed 120 characters")
        String department
) {
}
