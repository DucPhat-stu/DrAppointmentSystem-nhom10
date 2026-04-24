package com.healthcare.user.repository;

import com.healthcare.user.entity.MedicalRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicalRecordJpaRepository extends JpaRepository<MedicalRecordEntity, UUID> {
    List<MedicalRecordEntity> findByPatientIdOrderByVisitDateDesc(UUID patientId);

    Optional<MedicalRecordEntity> findByIdAndPatientId(UUID id, UUID patientId);
}
