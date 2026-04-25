package com.healthcare.doctor.web;

import com.healthcare.doctor.dto.TimeSlotResponse;
import com.healthcare.doctor.dto.UpdateTimeSlotStatusRequest;
import com.healthcare.doctor.service.TimeSlotService;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.web.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/time-slots")
public class InternalTimeSlotController {
    private final TimeSlotService timeSlotService;
    private final ApiResponseFactory apiResponseFactory;

    public InternalTimeSlotController(TimeSlotService timeSlotService, ApiResponseFactory apiResponseFactory) {
        this.timeSlotService = timeSlotService;
        this.apiResponseFactory = apiResponseFactory;
    }

    @PutMapping("/{slotId}/status")
    public ApiResponse<TimeSlotResponse> updateStatus(@PathVariable UUID slotId,
                                                      @Valid @RequestBody UpdateTimeSlotStatusRequest body) {
        return apiResponseFactory.success("Time slot status updated", timeSlotService.updateStatus(slotId, body.status()));
    }
}
