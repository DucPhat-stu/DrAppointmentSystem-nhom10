package com.healthcare.auth.application;

import java.util.Optional;

public interface UserCredentialReader {
    Optional<UserCredential> findByEmail(String email);
}

