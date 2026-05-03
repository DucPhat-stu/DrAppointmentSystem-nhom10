package com.healthcare.auth.repository;

import com.healthcare.auth.entity.AdminAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdminAuditLogJpaRepository extends JpaRepository<AdminAuditLogEntity, UUID> {
}
