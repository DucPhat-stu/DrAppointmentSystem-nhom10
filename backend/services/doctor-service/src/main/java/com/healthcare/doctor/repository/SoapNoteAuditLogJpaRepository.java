package com.healthcare.doctor.repository;

import com.healthcare.doctor.entity.SoapNoteAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SoapNoteAuditLogJpaRepository extends JpaRepository<SoapNoteAuditLogEntity, UUID> {
}
