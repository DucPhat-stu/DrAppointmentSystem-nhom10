package com.healthcare.auth.application;

import com.healthcare.shared.security.Permission;
import com.healthcare.shared.security.Role;

import java.util.Set;

public interface RolePermissionMapper {
    Set<Permission> map(Role role);
}

