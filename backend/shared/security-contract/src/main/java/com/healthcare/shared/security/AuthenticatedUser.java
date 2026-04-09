package com.healthcare.shared.security;

import java.util.UUID;

public record AuthenticatedUser(UUID userId, Role role) {
}

