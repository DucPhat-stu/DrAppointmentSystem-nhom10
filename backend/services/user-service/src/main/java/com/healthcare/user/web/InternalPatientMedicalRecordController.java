package com.healthcare.user.web;

import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.web.ApiResponseFactory;
import com.healthcare.user.dto.MedicalRecordResponse;
import com.healthcare.user.service.MedicalRecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/patients/{patientId}/medical-records")
public class InternalPatientMedicalRecordController {
    private final MedicalRecordService medicalRecordService;
    private final ApiResponseFactory apiResponseFactory;

    public InternalPatientMedicalRecordController(MedicalRecordService medicalRecordService,
                                                  ApiResponseFactory apiResponseFactory) {
        this.medicalRecordService = medicalRecordService;
        this.apiResponseFactory = apiResponseFactory;
    }

    @GetMapping
    public ApiResponse<List<MedicalRecordResponse>> list(@PathVariable UUID patientId) {
        return apiResponseFactory.success(
                "Patient medical records loaded",
                medicalRecordService.getRecordsByPatientUserId(patientId)
        );
    }
}
