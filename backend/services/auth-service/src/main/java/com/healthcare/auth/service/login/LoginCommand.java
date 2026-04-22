package com.healthcare.auth.service.login;

import com.healthcare.shared.security.Role;

public record LoginCommand(String email, String password, Role expectedRole) {
}
