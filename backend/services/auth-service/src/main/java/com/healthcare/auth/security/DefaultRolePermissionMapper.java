package com.healthcare.auth.security;

import com.healthcare.auth.service.token.RolePermissionMapper;
import com.healthcare.shared.security.Permission;
import com.healthcare.shared.security.Role;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class DefaultRolePermissionMapper implements RolePermissionMapper {
    @Override
    public Set<Permission> map(Role role) {
        return switch (role) {
            case PATIENT -> EnumSet.of(
                    Permission.AUTH_REFRESH,
                    Permission.AUTH_LOGOUT,
                    Permission.USER_PROFILE_READ,
                    Permission.USER_PROFILE_WRITE,
                    Permission.DOCTOR_DIRECTORY_READ,
                    Permission.DOCTOR_PROFILE_READ,
                    Permission.DOCTOR_TIMESLOT_READ,
                    Permission.APPOINTMENT_READ,
                    Permission.APPOINTMENT_BOOK,
                    Permission.APPOINTMENT_CANCEL,
                    Permission.APPOINTMENT_RESCHEDULE,
                    Permission.NOTIFICATION_READ
            );
            case DOCTOR -> EnumSet.of(
                    Permission.AUTH_REFRESH,
                    Permission.AUTH_LOGOUT,
                    Permission.USER_PROFILE_READ,
                    Permission.USER_PROFILE_WRITE,
                    Permission.DOCTOR_DIRECTORY_READ,
                    Permission.DOCTOR_PROFILE_READ,
                    Permission.DOCTOR_TIMESLOT_READ,
                    Permission.DOCTOR_TIMESLOT_WRITE,
                    Permission.APPOINTMENT_READ,
                    Permission.APPOINTMENT_CONFIRM,
                    Permission.NOTIFICATION_READ
            );
            case ADMIN, SUPER_ADMIN -> EnumSet.allOf(Permission.class);
        };
    }
}
