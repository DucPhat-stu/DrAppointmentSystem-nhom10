package com.healthcare.ai.web;

import com.healthcare.ai.dto.AICheckRequest;
import com.healthcare.ai.dto.StructuredAICheckRequest;
import com.healthcare.ai.service.AIConversationService;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.web.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
public class AIController {
    private final AIConversationService conversationService;
    private final ApiResponseFactory apiResponseFactory;

    public AIController(AIConversationService conversationService, ApiResponseFactory apiResponseFactory) {
        this.conversationService = conversationService;
        this.apiResponseFactory = apiResponseFactory;
    }

    @PostMapping("/check")
    public ApiResponse<String> checkSymptoms(@Valid @RequestBody AICheckRequest request) {
        return apiResponseFactory.success(
                "AI symptom check completed",
                conversationService.checkSymptoms(request.text())
        );
    }

    @PostMapping("/check/structured")
    public ApiResponse<String> checkStructuredSymptoms(@Valid @RequestBody StructuredAICheckRequest request) {
        return apiResponseFactory.success(
                "AI structured symptom check completed",
                conversationService.checkStructuredSymptoms(request)
        );
    }

    @PostMapping("/preview/structured")
    public ApiResponse<String> previewStructuredPrompt(@Valid @RequestBody StructuredAICheckRequest request) {
        return apiResponseFactory.success(
                "AI structured prompt preview completed",
                conversationService.previewStructuredPrompt(request)
        );
    }
}
