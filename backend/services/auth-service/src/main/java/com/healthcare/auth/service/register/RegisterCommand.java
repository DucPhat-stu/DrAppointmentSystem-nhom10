package com.healthcare.auth.service.register;

public record RegisterCommand(
        String name,
        String email,
        String phone,
        String password
) {
}
