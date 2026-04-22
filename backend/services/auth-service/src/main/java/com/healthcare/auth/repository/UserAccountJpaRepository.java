package com.healthcare.auth.repository;

import com.healthcare.auth.entity.UserAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountJpaRepository extends JpaRepository<UserAccountEntity, UUID> {
    Optional<UserAccountEntity> findByEmailIgnoreCase(String email);
}
