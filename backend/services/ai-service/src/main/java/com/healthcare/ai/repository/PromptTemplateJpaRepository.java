package com.healthcare.ai.repository;

import com.healthcare.ai.entity.PromptTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface PromptTemplateJpaRepository extends JpaRepository<PromptTemplateEntity, UUID> {
    Optional<PromptTemplateEntity> findFirstByActiveTrue();

    @Modifying
    @Query("UPDATE PromptTemplateEntity t SET t.active = false WHERE t.active = true")
    void deactivateAll();
}
