package com.healthcare.auth.service.token;

import java.util.Optional;

public interface RefreshTokenReader {
    Optional<RefreshTokenSession> findByToken(String token);
}
