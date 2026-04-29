package com.healthcare.ai.repository;

import com.healthcare.ai.entity.PromptTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PromptTemplateJpaRepository extends JpaRepository<PromptTemplateEntity, UUID> {
    Optional<PromptTemplateEntity> findFirstByActiveTrue();
}
