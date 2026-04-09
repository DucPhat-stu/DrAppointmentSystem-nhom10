package com.healthcare.auth.application;

public interface RefreshTokenWriter {
    void save(RefreshTokenRecord refreshTokenRecord);
}

