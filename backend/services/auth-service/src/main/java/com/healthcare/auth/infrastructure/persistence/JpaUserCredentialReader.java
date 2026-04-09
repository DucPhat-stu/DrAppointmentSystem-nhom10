package com.healthcare.auth.infrastructure.persistence;

import com.healthcare.auth.application.UserCredential;
import com.healthcare.auth.application.UserCredentialReader;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class JpaUserCredentialReader implements UserCredentialReader {
    private final UserAccountJpaRepository userAccountJpaRepository;

    public JpaUserCredentialReader(UserAccountJpaRepository userAccountJpaRepository) {
        this.userAccountJpaRepository = userAccountJpaRepository;
    }

    @Override
    public Optional<UserCredential> findByEmail(String email) {
        return userAccountJpaRepository.findByEmailIgnoreCase(email)
                .map(this::toUserCredential);
    }

    @Override
    public Optional<UserCredential> findById(UUID userId) {
        return userAccountJpaRepository.findById(userId)
                .map(this::toUserCredential);
    }

    private UserCredential toUserCredential(UserAccountEntity user) {
        return new UserCredential(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole(),
                user.getStatus()
        );
    }
}
