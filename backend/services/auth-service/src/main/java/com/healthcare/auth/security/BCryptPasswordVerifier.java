package com.healthcare.auth.security;

import com.healthcare.auth.service.login.PasswordVerifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordVerifier implements PasswordVerifier {
    private final PasswordEncoder passwordEncoder;

    public BCryptPasswordVerifier(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        return passwordEncoder.matches(rawPassword, passwordHash);
    }
}
