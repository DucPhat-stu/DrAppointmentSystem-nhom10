package com.healthcare.auth.service.token;

public interface RefreshTokenWriter {
    void save(RefreshTokenRecord refreshTokenRecord);
}
