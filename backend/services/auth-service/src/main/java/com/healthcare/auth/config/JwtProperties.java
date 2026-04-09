package com.healthcare.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    private String secret;
    private long accessTokenExpiresInSeconds;
    private long refreshTokenExpiresInSeconds;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessTokenExpiresInSeconds() {
        return accessTokenExpiresInSeconds;
    }

    public void setAccessTokenExpiresInSeconds(long accessTokenExpiresInSeconds) {
        this.accessTokenExpiresInSeconds = accessTokenExpiresInSeconds;
    }

    public long getRefreshTokenExpiresInSeconds() {
        return refreshTokenExpiresInSeconds;
    }

    public void setRefreshTokenExpiresInSeconds(long refreshTokenExpiresInSeconds) {
        this.refreshTokenExpiresInSeconds = refreshTokenExpiresInSeconds;
    }
}

