package com.healthcare.user.web;

import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import com.healthcare.shared.common.web.ApiResponseFactory;
import com.healthcare.shared.security.ForwardedHeaders;
import com.healthcare.user.dto.CreateMedicalRecordRequest;
import com.healthcare.user.dto.MedicalRecordResponse;
import com.healthcare.user.service.MedicalRecordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/medical-records")
public class MedicalRecordController {
    private final MedicalRecordService medicalRecordService;
    private final ApiResponseFactory apiResponseFactory;

    public MedicalRecordController(MedicalRecordService medicalRecordService,
                                    ApiResponseFactory apiResponseFactory) {
        this.medicalRecordService = medicalRecordService;
        this.apiResponseFactory = apiResponseFactory;
    }

    /**
     * GET /api/v1/medical-records — patient views their records.
     */
    @GetMapping
    public ApiResponse<List<MedicalRecordResponse>> getMyRecords(HttpServletRequest request) {
        UUID userId = extractUserId(request);
        List<MedicalRecordResponse> records = medicalRecordService.getRecords(userId);
        return apiResponseFactory.success("Medical records loaded", records);
    }

    /**
     * GET /api/v1/medical-records/{id} — patient views a single record detail.
     */
    @GetMapping("/{id}")
    public ApiResponse<MedicalRecordResponse> getRecordDetail(HttpServletRequest request,
                                                               @PathVariable UUID id) {
        UUID userId = extractUserId(request);
        MedicalRecordResponse record = medicalRecordService.getRecordDetail(userId, id);
        return apiResponseFactory.success("Record detail loaded", record);
    }

    /**
     * POST /api/v1/medical-records — doctor creates a medical record for a patient.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> createRecord(
            HttpServletRequest request,
            @Valid @RequestBody CreateMedicalRecordRequest body) {

        String role = (String) request.getAttribute(ForwardedHeaders.USER_ROLE);
        if (!"DOCTOR".equals(role)) {
            throw new ApiException(ErrorCode.INSUFFICIENT_PERMISSIONS,
                    "Only doctors can create medical records");
        }

        UUID doctorUserId = extractUserId(request);
        // In MVP, doctor name comes from profile or can be extracted.
        // For simplicity, we'll use a header or default.
        String email = (String) request.getAttribute("X-User-Email");
        String doctorName = email != null ? email : "Doctor";

        MedicalRecordResponse record = medicalRecordService.createRecord(
                doctorUserId, doctorName, body);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(apiResponseFactory.success("Medical record created", record));
    }

    private UUID extractUserId(HttpServletRequest request) {
        String userIdStr = (String) request.getAttribute(ForwardedHeaders.USER_ID);
        return UUID.fromString(userIdStr);
    }
}
