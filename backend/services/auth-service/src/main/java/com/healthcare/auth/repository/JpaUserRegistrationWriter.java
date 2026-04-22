package com.healthcare.auth.repository;

import com.healthcare.auth.entity.UserAccountEntity;
import com.healthcare.auth.service.register.NewUserAccount;
import com.healthcare.auth.service.register.UserRegistrationWriter;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class JpaUserRegistrationWriter implements UserRegistrationWriter {
    private final UserAccountJpaRepository userAccountJpaRepository;

    public JpaUserRegistrationWriter(UserAccountJpaRepository userAccountJpaRepository) {
        this.userAccountJpaRepository = userAccountJpaRepository;
    }

    @Override
    public void save(NewUserAccount userAccount) {
        UserAccountEntity entity = new UserAccountEntity();
        entity.setId(userAccount.userId());
        entity.setEmail(userAccount.email());
        entity.setPasswordHash(userAccount.passwordHash());
        entity.setRole(userAccount.role());
        entity.setStatus(userAccount.status());
        entity.setFullName(userAccount.fullName());
        entity.setPhone(userAccount.phone());
        entity.setFailedLoginAttempts(0);
        entity.setLastLoginAt(null);
        entity.setCreatedAt(userAccount.createdAt());
        entity.setUpdatedAt(userAccount.createdAt());

        try {
            userAccountJpaRepository.save(entity);
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(ErrorCode.CONFLICT, "Email already exists");
        }
    }
}
