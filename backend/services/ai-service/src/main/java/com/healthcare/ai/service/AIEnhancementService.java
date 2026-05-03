package com.healthcare.ai.service;

import com.healthcare.ai.dto.AIConversationResponse;
import com.healthcare.ai.dto.AIFeedbackRequest;
import com.healthcare.ai.dto.AIMessageResponse;
import com.healthcare.ai.dto.DoctorRecommendationResponse;
import com.healthcare.ai.dto.ImageAnalysisResponse;
import com.healthcare.ai.entity.AIConversationEntity;
import com.healthcare.ai.entity.AIFeedbackEntity;
import com.healthcare.ai.entity.AIMessageEntity;
import com.healthcare.ai.repository.AIConversationJpaRepository;
import com.healthcare.ai.repository.AIFeedbackJpaRepository;
import com.healthcare.ai.repository.AIMessageJpaRepository;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class AIEnhancementService {
    private final AIConversationJpaRepository conversationRepository;
    private final AIMessageJpaRepository messageRepository;
    private final AIFeedbackJpaRepository feedbackRepository;

    public AIEnhancementService(AIConversationJpaRepository conversationRepository,
                                AIMessageJpaRepository messageRepository,
                                AIFeedbackJpaRepository feedbackRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @Transactional
    public void saveExchange(UUID userId, String userText, String assistantText) {
        if (userId == null) return;
        AIConversationEntity conversation = new AIConversationEntity();
        conversation.setUserId(userId);
        conversation.setTitle(titleFrom(userText));
        AIConversationEntity saved = conversationRepository.save(conversation);
        messageRepository.save(message(saved.getId(), "USER", userText));
        messageRepository.save(message(saved.getId(), "ASSISTANT", assistantText));
    }

    @Transactional(readOnly = true)
    public List<AIConversationResponse> listHistory(UUID userId) {
        return conversationRepository.findTop20ByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(conversation -> new AIConversationResponse(
                        conversation.getId(),
                        conversation.getTitle(),
                        conversation.getCreatedAt(),
                        conversation.getUpdatedAt(),
                        messageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversation.getId()).stream()
                                .map(message -> new AIMessageResponse(
                                        message.getId(),
                                        message.getRole(),
                                        message.getContent(),
                                        message.getCreatedAt()
                                ))
                                .toList()
                ))
                .toList();
    }

    @Transactional
    public void saveFeedback(UUID userId, AIFeedbackRequest request) {
        if (!messageRepository.existsById(request.messageId())) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "AI message not found");
        }
        AIFeedbackEntity entity = new AIFeedbackEntity();
        entity.setUserId(userId);
        entity.setMessageId(request.messageId());
        entity.setRating(request.rating());
        entity.setComment(request.comment());
        feedbackRepository.save(entity);
    }

    public List<DoctorRecommendationResponse> recommendDoctors(String symptoms) {
        String lower = symptoms.toLowerCase();
        if (lower.contains("tim") || lower.contains("nguc") || lower.contains("chest")) {
            return List.of(new DoctorRecommendationResponse("Doctor Seed", "Cardiology", "Symptoms mention chest or heart-related risk.", 0.91));
        }
        if (lower.contains("da") || lower.contains("rash")) {
            return List.of(new DoctorRecommendationResponse("Doctor Seed", "Dermatology", "Skin symptoms match dermatology triage.", 0.87));
        }
        return List.of(
                new DoctorRecommendationResponse("Doctor Seed", "General Medicine", "Good first contact for unclear symptoms.", 0.82),
                new DoctorRecommendationResponse("Doctor Seed", "Internal Medicine", "Can evaluate multi-system symptoms.", 0.78)
        );
    }

    public ImageAnalysisResponse analyzeImage(MultipartFile file) {
        String name = file == null || file.getOriginalFilename() == null ? "medical image" : file.getOriginalFilename();
        return new ImageAnalysisResponse(
                name.toLowerCase().contains("xray") ? "X-ray" : "Uploaded medical image",
                "No critical abnormality detected in mock analysis.",
                "Ask a clinician to review the original image before making care decisions.",
                0.74
        );
    }

    private AIMessageEntity message(UUID conversationId, String role, String content) {
        AIMessageEntity entity = new AIMessageEntity();
        entity.setConversationId(conversationId);
        entity.setRole(role);
        entity.setContent(content);
        return entity;
    }

    private String titleFrom(String text) {
        if (text == null || text.isBlank()) return "AI consultation";
        String trimmed = text.trim();
        return trimmed.length() <= 80 ? trimmed : trimmed.substring(0, 77) + "...";
    }
}
