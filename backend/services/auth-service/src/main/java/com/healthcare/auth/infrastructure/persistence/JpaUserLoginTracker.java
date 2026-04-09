package com.healthcare.auth.infrastructure.persistence;

import com.healthcare.auth.application.UserLoginTracker;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class JpaUserLoginTracker implements UserLoginTracker {
    private final UserAccountJpaRepository userAccountJpaRepository;

    public JpaUserLoginTracker(UserAccountJpaRepository userAccountJpaRepository) {
        this.userAccountJpaRepository = userAccountJpaRepository;
    }

    @Override
    @Transactional
    public void markSuccessfulLogin(UUID userId, OffsetDateTime loggedInAt) {
        UserAccountEntity user = userAccountJpaRepository.findById(userId)
                .orElseThrow();
        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(loggedInAt);
        userAccountJpaRepository.save(user);
    }
}

