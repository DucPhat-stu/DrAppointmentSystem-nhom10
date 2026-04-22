package com.healthcare.auth.service.login;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface UserLoginTracker {
    void markSuccessfulLogin(UUID userId, OffsetDateTime loggedInAt);

    void markFailedLoginAttempt(UUID userId);
}
