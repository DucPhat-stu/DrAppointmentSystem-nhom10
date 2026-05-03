package com.healthcare.auth.web;

import com.healthcare.auth.dto.AdminUserDto;
import com.healthcare.auth.dto.AdminUserPageDto;
import com.healthcare.auth.entity.AdminAuditLogEntity;
import com.healthcare.auth.entity.UserAccountEntity;
import com.healthcare.auth.entity.UserStatus;
import com.healthcare.auth.repository.AdminAuditLogJpaRepository;
import com.healthcare.auth.repository.UserAccountJpaRepository;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import com.healthcare.shared.common.web.ApiResponseFactory;
import com.healthcare.shared.security.ForwardedHeaders;
import com.healthcare.shared.security.Role;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final UserAccountJpaRepository userRepository;
    private final AdminAuditLogJpaRepository auditLogRepository;
    private final ApiResponseFactory apiResponseFactory;

    public AdminUserController(UserAccountJpaRepository userRepository,
                               AdminAuditLogJpaRepository auditLogRepository,
                               ApiResponseFactory apiResponseFactory) {
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.apiResponseFactory = apiResponseFactory;
    }

    /**
     * GET /api/v1/admin/users
     * Returns a paginated list of all users.
     * Query params: page, size, role, status
     */
    @GetMapping
    public ApiResponse<AdminUserPageDto> listUsers(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        resolveAdmin(request); // RBAC guard

        PageRequest pageRequest = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        Page<UserAccountEntity> result = userRepository.findAll(buildFilter(role, status), pageRequest);
        AdminUserPageDto body = new AdminUserPageDto(
                result.getContent().stream().map(this::toDto).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
        return apiResponseFactory.success("Users loaded", body);
    }

    /**
     * PUT /api/v1/admin/users/{id}/disable
     * Disables a user account (sets status to INACTIVE).
     */
    @PutMapping("/{id}/disable")
    public ApiResponse<AdminUserDto> disableUser(HttpServletRequest request,
                                                 @PathVariable UUID id) {
        UUID adminId = resolveAdmin(request);

        UserAccountEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        if (user.getRole() == Role.ADMIN || user.getRole() == Role.SUPER_ADMIN) {
            throw new ApiException(ErrorCode.INSUFFICIENT_PERMISSIONS, "Cannot disable another admin account");
        }

        if (user.getStatus() != UserStatus.INACTIVE) {
            user.setStatus(UserStatus.INACTIVE);
            user.setUpdatedAt(OffsetDateTime.now());
            userRepository.save(user);

            auditLogRepository.save(AdminAuditLogEntity.of(
                    adminId,
                    "DISABLE_USER",
                    "USER",
                    id,
                    "User " + user.getEmail() + " disabled by admin"
            ));
        }

        return apiResponseFactory.success("User disabled", toDto(user));
    }

    /**
     * PUT /api/v1/admin/users/{id}/enable
     * Re-enables a previously disabled user account.
     */
    @PutMapping("/{id}/enable")
    public ApiResponse<AdminUserDto> enableUser(HttpServletRequest request,
                                                @PathVariable UUID id) {
        UUID adminId = resolveAdmin(request);

        UserAccountEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            user.setStatus(UserStatus.ACTIVE);
            user.setUpdatedAt(OffsetDateTime.now());
            userRepository.save(user);

            auditLogRepository.save(AdminAuditLogEntity.of(
                    adminId,
                    "ENABLE_USER",
                    "USER",
                    id,
                    "User " + user.getEmail() + " re-enabled by admin"
            ));
        }

        return apiResponseFactory.success("User enabled", toDto(user));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /** Reads userId from JWT-injected request attributes and validates ADMIN role. */
    private UUID resolveAdmin(HttpServletRequest request) {
        Object roleAttr = request.getAttribute(ForwardedHeaders.USER_ROLE);
        if (!(roleAttr instanceof String rawRole)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Missing authenticated user context");
        }
        Role role;
        try {
            role = Role.valueOf(rawRole);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid user role");
        }
        if (role != Role.ADMIN && role != Role.SUPER_ADMIN) {
            throw new ApiException(ErrorCode.INSUFFICIENT_PERMISSIONS, "Admin role is required");
        }

        Object userIdAttr = request.getAttribute(ForwardedHeaders.USER_ID);
        if (!(userIdAttr instanceof String rawId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Missing user id context");
        }
        try {
            return UUID.fromString(rawId);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid user id");
        }
    }

    private Specification<UserAccountEntity> buildFilter(String rawRole, String rawStatus) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (rawRole != null && !rawRole.isBlank()) {
                try {
                    Role parsedRole = Role.valueOf(rawRole.trim().toUpperCase());
                    predicates.add(cb.equal(root.get("role"), parsedRole));
                } catch (IllegalArgumentException ignored) {
                    // ignore invalid role filter
                }
            }
            if (rawStatus != null && !rawStatus.isBlank()) {
                try {
                    UserStatus parsedStatus = UserStatus.valueOf(rawStatus.trim().toUpperCase());
                    predicates.add(cb.equal(root.get("status"), parsedStatus));
                } catch (IllegalArgumentException ignored) {
                    // ignore invalid status filter
                }
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private AdminUserDto toDto(UserAccountEntity e) {
        return new AdminUserDto(
                e.getId(),
                e.getEmail(),
                e.getFullName(),
                e.getPhone(),
                e.getRole(),
                e.getStatus(),
                e.getFailedLoginAttempts(),
                e.getLastLoginAt(),
                e.getCreatedAt()
        );
    }
}
