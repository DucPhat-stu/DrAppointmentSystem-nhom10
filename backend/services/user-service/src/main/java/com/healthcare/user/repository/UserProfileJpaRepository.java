package com.healthcare.user.repository;

import com.healthcare.user.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserProfileJpaRepository extends JpaRepository<UserProfileEntity, UUID> {
    Optional<UserProfileEntity> findByUserId(UUID userId);

    List<UserProfileEntity> findAllByUserIdIn(Collection<UUID> userIds);
}
