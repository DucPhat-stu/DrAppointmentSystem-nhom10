package com.healthcare.ai.repository;

import com.healthcare.ai.entity.AIConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AIConversationJpaRepository extends JpaRepository<AIConversationEntity, UUID> {
    List<AIConversationEntity> findTop20ByUserIdOrderByUpdatedAtDesc(UUID userId);
}
