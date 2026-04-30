package com.healthcare.ai.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class JwtSecretValidator {
    public static final String DEFAULT_SECRET = "change-this-before-shared-environments";

    private final JwtProperties jwtProperties;

    public JwtSecretValidator(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    void validate() {
        String secret = jwtProperties.getSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET must be configured for ai-service");
        }

        if (DEFAULT_SECRET.equals(secret)) {
            throw new IllegalStateException("JWT_SECRET must not use the default ai-service value");
        }
    }
}
