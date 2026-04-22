package com.healthcare.auth.dto.request;

import com.healthcare.shared.security.Role;

public enum LoginActor {
    PATIENT(Role.PATIENT),
    DOCTOR(Role.DOCTOR);

    private final Role role;

    LoginActor(Role role) {
        this.role = role;
    }

    public Role toRole() {
        return role;
    }
}
