package com.healthcare.doctor.web;

import com.healthcare.doctor.dto.AvailableSlotResponse;
import com.healthcare.doctor.service.AvailableSlotService;
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
@RequestMapping("/api/v1/doctors/{doctorId}/available-slots")
public class AvailableSlotController {
    private final AvailableSlotService availableSlotService;
    private final ApiResponseFactory apiResponseFactory;

    public AvailableSlotController(AvailableSlotService availableSlotService,
                                   ApiResponseFactory apiResponseFactory) {
        this.availableSlotService = availableSlotService;
        this.apiResponseFactory = apiResponseFactory;
    }

    @GetMapping
    public ApiResponse<List<AvailableSlotResponse>> list(
            @PathVariable UUID doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return apiResponseFactory.success(
                "Available slots loaded",
                availableSlotService.find(doctorId, date)
        );
    }
}
