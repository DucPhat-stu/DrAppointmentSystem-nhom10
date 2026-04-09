package com.healthcare.auth.application;

import java.util.Optional;

public interface RefreshTokenReader {
    Optional<RefreshTokenSession> findByToken(String token);
}

