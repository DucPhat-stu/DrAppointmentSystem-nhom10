package com.healthcare.auth.application;

import java.util.Optional;
import java.util.UUID;

public interface UserCredentialReader {
    Optional<UserCredential> findByEmail(String email);

    Optional<UserCredential> findById(UUID userId);
}
