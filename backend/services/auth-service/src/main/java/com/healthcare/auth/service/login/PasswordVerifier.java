package com.healthcare.auth.service.login;

public interface PasswordVerifier {
    boolean matches(String rawPassword, String passwordHash);
}
