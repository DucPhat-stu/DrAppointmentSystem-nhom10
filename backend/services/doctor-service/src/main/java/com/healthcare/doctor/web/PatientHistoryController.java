package com.healthcare.doctor.web;

import com.healthcare.doctor.dto.PatientHistoryResponse;
import com.healthcare.doctor.security.CurrentDoctorResolver;
import com.healthcare.doctor.service.PatientHistoryService;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.web.ApiResponseFactory;
import com.healthcare.shared.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctors/patients")
public class PatientHistoryController {
    private final PatientHistoryService patientHistoryService;
    private final CurrentDoctorResolver currentDoctorResolver;
    private final ApiResponseFactory apiResponseFactory;

    public PatientHistoryController(PatientHistoryService patientHistoryService,
                                    CurrentDoctorResolver currentDoctorResolver,
                                    ApiResponseFactory apiResponseFactory) {
        this.patientHistoryService = patientHistoryService;
        this.currentDoctorResolver = currentDoctorResolver;
        this.apiResponseFactory = apiResponseFactory;
    }

    @GetMapping("/{patientId}/history")
    public ApiResponse<PatientHistoryResponse> history(
            HttpServletRequest request,
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        AuthenticatedUser doctor = currentDoctorResolver.resolve(request);
        return apiResponseFactory.success(
                "Patient history loaded",
                patientHistoryService.find(doctor.userId(), patientId, page, size)
        );
    }
}
