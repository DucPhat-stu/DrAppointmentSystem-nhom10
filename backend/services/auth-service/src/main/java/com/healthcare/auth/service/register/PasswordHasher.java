package com.healthcare.auth.service.register;

public interface PasswordHasher {
    String hash(String rawPassword);
}
