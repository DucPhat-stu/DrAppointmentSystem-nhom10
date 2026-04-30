package com.healthcare.ai.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtSecretValidatorTest {
    @Test
    void rejectsDefaultSecret() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(JwtSecretValidator.DEFAULT_SECRET);

        JwtSecretValidator validator = new JwtSecretValidator(properties);

        assertThatThrownBy(validator::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("default");
    }

    @Test
    void acceptsConfiguredSecret() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("local-development-secret-that-is-not-the-default");

        JwtSecretValidator validator = new JwtSecretValidator(properties);

        assertThatCode(validator::validate).doesNotThrowAnyException();
    }
}
