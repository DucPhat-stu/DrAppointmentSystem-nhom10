package com.healthcare.auth.repository;

import com.healthcare.auth.entity.UserAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountJpaRepository extends JpaRepository<UserAccountEntity, UUID>,
        JpaSpecificationExecutor<UserAccountEntity> {
    Optional<UserAccountEntity> findByEmailIgnoreCase(String email);

    Optional<UserAccountEntity> findByPhone(String phone);

    Optional<UserAccountEntity> findByPasswordResetToken(String passwordResetToken);

    Optional<UserAccountEntity> findByDoctorCodeIgnoreCase(String doctorCode);
}
