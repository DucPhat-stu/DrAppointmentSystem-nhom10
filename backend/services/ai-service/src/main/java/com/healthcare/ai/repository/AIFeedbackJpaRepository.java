package com.healthcare.ai.repository;

import com.healthcare.ai.entity.AIFeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AIFeedbackJpaRepository extends JpaRepository<AIFeedbackEntity, UUID> {
}
