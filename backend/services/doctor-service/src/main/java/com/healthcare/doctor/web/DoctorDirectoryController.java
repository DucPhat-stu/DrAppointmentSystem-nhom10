package com.healthcare.doctor.web;

import com.healthcare.doctor.dto.DoctorAvailabilityResponse;
import com.healthcare.doctor.dto.DoctorProfileSummaryResponse;
import com.healthcare.doctor.service.DoctorDirectoryService;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.web.ApiResponseFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctors")
public class DoctorDirectoryController {
    private final DoctorDirectoryService doctorDirectoryService;
    private final ApiResponseFactory apiResponseFactory;

    public DoctorDirectoryController(DoctorDirectoryService doctorDirectoryService,
                                     ApiResponseFactory apiResponseFactory) {
        this.doctorDirectoryService = doctorDirectoryService;
        this.apiResponseFactory = apiResponseFactory;
    }

    @GetMapping
    public ApiResponse<List<DoctorAvailabilityResponse>> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return apiResponseFactory.success(
                "Available doctors loaded",
                doctorDirectoryService.findAvailableDoctors(date)
        );
    }

    @GetMapping("/{doctorId}")
    public ApiResponse<DoctorProfileSummaryResponse> get(@PathVariable UUID doctorId) {
        return apiResponseFactory.success(
                "Doctor profile loaded",
                doctorDirectoryService.getDoctor(doctorId)
        );
    }
}
