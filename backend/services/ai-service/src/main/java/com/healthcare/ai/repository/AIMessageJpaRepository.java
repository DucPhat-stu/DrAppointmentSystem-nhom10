package com.healthcare.ai.repository;

import com.healthcare.ai.entity.AIMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AIMessageJpaRepository extends JpaRepository<AIMessageEntity, UUID> {
    List<AIMessageEntity> findAllByConversationIdOrderByCreatedAtAsc(UUID conversationId);
}
