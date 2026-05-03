package com.healthcare.ai.web;

import com.healthcare.ai.dto.AICheckRequest;
import com.healthcare.ai.dto.AIConversationResponse;
import com.healthcare.ai.dto.AIFeedbackRequest;
import com.healthcare.ai.dto.DoctorRecommendationRequest;
import com.healthcare.ai.dto.DoctorRecommendationResponse;
import com.healthcare.ai.dto.DiseaseTrendResponse;
import com.healthcare.ai.dto.FollowUpSuggestionRequest;
import com.healthcare.ai.dto.FollowUpSuggestionResponse;
import com.healthcare.ai.dto.HealthRiskAlertRequest;
import com.healthcare.ai.dto.HealthRiskAlertResponse;
import com.healthcare.ai.dto.ImageAnalysisResponse;
import com.healthcare.ai.dto.StructuredAICheckRequest;
import com.healthcare.ai.dto.WaitTimePredictionRequest;
import com.healthcare.ai.dto.WaitTimePredictionResponse;
import com.healthcare.ai.service.AIEnhancementService;
import com.healthcare.ai.service.AIConversationService;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.web.ApiResponseFactory;
import com.healthcare.shared.security.ForwardedHeaders;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
public class AIController {
    private final AIConversationService conversationService;
    private final AIEnhancementService enhancementService;
    private final ApiResponseFactory apiResponseFactory;

    public AIController(AIConversationService conversationService,
                        AIEnhancementService enhancementService,
                        ApiResponseFactory apiResponseFactory) {
        this.conversationService = conversationService;
        this.enhancementService = enhancementService;
        this.apiResponseFactory = apiResponseFactory;
    }

    @PostMapping("/check")
    public ApiResponse<String> checkSymptoms(HttpServletRequest httpRequest, @Valid @RequestBody AICheckRequest request) {
        String result = conversationService.checkSymptoms(request.text());
        enhancementService.saveExchange(currentUserId(httpRequest), request.text(), result);
        return apiResponseFactory.success(
                "AI symptom check completed",
                result
        );
    }

    @PostMapping("/check/structured")
    public ApiResponse<String> checkStructuredSymptoms(HttpServletRequest httpRequest, @Valid @RequestBody StructuredAICheckRequest request) {
        String result = conversationService.checkStructuredSymptoms(request);
        enhancementService.saveExchange(currentUserId(httpRequest), request.symptoms(), result);
        return apiResponseFactory.success(
                "AI structured symptom check completed",
                result
        );
    }

    @GetMapping("/conversations")
    public ApiResponse<List<AIConversationResponse>> listConversations(HttpServletRequest request) {
        return apiResponseFactory.success("AI conversations loaded", enhancementService.listHistory(currentUserId(request)));
    }

    @PostMapping("/feedback")
    public ApiResponse<Void> feedback(HttpServletRequest request, @Valid @RequestBody AIFeedbackRequest body) {
        enhancementService.saveFeedback(currentUserId(request), body);
        return apiResponseFactory.success("AI feedback saved", null);
    }

    @PostMapping("/doctor-recommendations")
    public ApiResponse<List<DoctorRecommendationResponse>> recommendDoctors(@Valid @RequestBody DoctorRecommendationRequest request) {
        return apiResponseFactory.success("Doctor recommendations generated", enhancementService.recommendDoctors(request.symptoms()));
    }

    @PostMapping(value = "/image-analysis", consumes = "multipart/form-data")
    public ApiResponse<ImageAnalysisResponse> analyzeImage(@RequestPart("file") MultipartFile file) {
        return apiResponseFactory.success("Medical image mock analysis completed", enhancementService.analyzeImage(file));
    }

    @PostMapping("/follow-up-suggestion")
    public ApiResponse<FollowUpSuggestionResponse> suggestFollowUp(@Valid @RequestBody FollowUpSuggestionRequest request) {
        return apiResponseFactory.success("Follow-up suggestion generated", enhancementService.suggestFollowUp(request.diagnosis()));
    }

    @PostMapping("/wait-time")
    public ApiResponse<WaitTimePredictionResponse> predictWaitTime(@RequestBody WaitTimePredictionRequest request) {
        return apiResponseFactory.success("Wait time prediction generated", enhancementService.predictWaitTime(request.department()));
    }

    @GetMapping("/analytics/disease-trends")
    public ApiResponse<List<DiseaseTrendResponse>> diseaseTrends() {
        return apiResponseFactory.success("Disease trends loaded", enhancementService.diseaseTrends());
    }

    @PostMapping("/risk-alerts")
    public ApiResponse<HealthRiskAlertResponse> healthRisk(@Valid @RequestBody HealthRiskAlertRequest request) {
        return apiResponseFactory.success("Health risk alerts generated", enhancementService.healthRisk(request.symptoms(), request.age()));
    }

    @PostMapping("/preview/structured")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> previewStructuredPrompt(@Valid @RequestBody StructuredAICheckRequest request) {
        return apiResponseFactory.success(
                "AI structured prompt preview completed",
                conversationService.previewStructuredPrompt(request)
        );
    }

    private UUID currentUserId(HttpServletRequest request) {
        return UUID.fromString((String) request.getAttribute(ForwardedHeaders.USER_ID));
    }
}
