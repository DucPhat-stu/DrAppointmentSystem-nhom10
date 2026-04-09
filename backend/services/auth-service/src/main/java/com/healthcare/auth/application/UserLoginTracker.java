package com.healthcare.auth.application;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface UserLoginTracker {
    void markSuccessfulLogin(UUID userId, OffsetDateTime loggedInAt);
}

