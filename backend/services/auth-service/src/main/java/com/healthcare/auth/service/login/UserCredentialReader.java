package com.healthcare.auth.service.login;

import java.util.Optional;
import java.util.UUID;

public interface UserCredentialReader {
    Optional<UserCredential> findByEmail(String email);

    Optional<UserCredential> findById(UUID userId);
}
