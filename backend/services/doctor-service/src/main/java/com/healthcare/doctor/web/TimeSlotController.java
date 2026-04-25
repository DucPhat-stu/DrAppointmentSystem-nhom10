package com.healthcare.doctor.web;

import com.healthcare.doctor.dto.TimeSlotRequest;
import com.healthcare.doctor.dto.TimeSlotResponse;
import com.healthcare.doctor.security.CurrentDoctorResolver;
import com.healthcare.doctor.service.TimeSlotService;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.web.ApiResponseFactory;
import com.healthcare.shared.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctors/time-slots")
public class TimeSlotController {
    private final TimeSlotService timeSlotService;
    private final CurrentDoctorResolver currentDoctorResolver;
    private final ApiResponseFactory apiResponseFactory;

    public TimeSlotController(TimeSlotService timeSlotService,
                              CurrentDoctorResolver currentDoctorResolver,
                              ApiResponseFactory apiResponseFactory) {
        this.timeSlotService = timeSlotService;
        this.currentDoctorResolver = currentDoctorResolver;
        this.apiResponseFactory = apiResponseFactory;
    }

    @PostMapping
    public ApiResponse<TimeSlotResponse> create(HttpServletRequest request,
                                                @Valid @RequestBody TimeSlotRequest body) {
        AuthenticatedUser doctor = currentDoctorResolver.resolve(request);
        return apiResponseFactory.success("Time slot created", timeSlotService.create(doctor.userId(), body));
    }

    @PutMapping("/{id}")
    public ApiResponse<TimeSlotResponse> update(HttpServletRequest request,
                                                @PathVariable UUID id,
                                                @Valid @RequestBody TimeSlotRequest body) {
        AuthenticatedUser doctor = currentDoctorResolver.resolve(request);
        return apiResponseFactory.success("Time slot updated", timeSlotService.update(doctor.userId(), id, body));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(HttpServletRequest request, @PathVariable UUID id) {
        AuthenticatedUser doctor = currentDoctorResolver.resolve(request);
        timeSlotService.delete(doctor.userId(), id);
        return apiResponseFactory.success("Time slot deleted", null);
    }
}
