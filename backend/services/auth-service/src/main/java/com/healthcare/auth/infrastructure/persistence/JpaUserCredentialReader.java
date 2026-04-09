package com.healthcare.auth.infrastructure.persistence;

import com.healthcare.auth.application.UserCredential;
import com.healthcare.auth.application.UserCredentialReader;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaUserCredentialReader implements UserCredentialReader {
    private final UserAccountJpaRepository userAccountJpaRepository;

    public JpaUserCredentialReader(UserAccountJpaRepository userAccountJpaRepository) {
        this.userAccountJpaRepository = userAccountJpaRepository;
    }

    @Override
    public Optional<UserCredential> findByEmail(String email) {
        return userAccountJpaRepository.findByEmailIgnoreCase(email)
                .map(user -> new UserCredential(
                        user.getId(),
                        user.getEmail(),
                        user.getPasswordHash(),
                        user.getRole(),
                        user.getStatus()
                ));
    }
}

