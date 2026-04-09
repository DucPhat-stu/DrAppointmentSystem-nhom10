package com.healthcare.auth.application;

public interface PasswordVerifier {
    boolean matches(String rawPassword, String passwordHash);
}

