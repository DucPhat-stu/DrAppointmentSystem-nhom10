package com.healthcare.user.repository;

import com.healthcare.user.entity.DoctorCertificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DoctorCertificationJpaRepository extends JpaRepository<DoctorCertificationEntity, UUID> {
    List<DoctorCertificationEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<DoctorCertificationEntity> findByIdAndUserId(UUID id, UUID userId);
}
