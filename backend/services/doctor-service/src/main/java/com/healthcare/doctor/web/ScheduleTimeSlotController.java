package com.healthcare.doctor.web;

import com.healthcare.doctor.dto.TimeSlotResponse;
import com.healthcare.doctor.security.CurrentDoctorResolver;
import com.healthcare.doctor.service.TimeSlotService;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.web.ApiResponseFactory;
import com.healthcare.shared.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctors/schedules/{scheduleId}/time-slots")
public class ScheduleTimeSlotController {
    private final TimeSlotService timeSlotService;
    private final CurrentDoctorResolver currentDoctorResolver;
    private final ApiResponseFactory apiResponseFactory;

    public ScheduleTimeSlotController(TimeSlotService timeSlotService,
                                      CurrentDoctorResolver currentDoctorResolver,
                                      ApiResponseFactory apiResponseFactory) {
        this.timeSlotService = timeSlotService;
        this.currentDoctorResolver = currentDoctorResolver;
        this.apiResponseFactory = apiResponseFactory;
    }

    @GetMapping
    public ApiResponse<List<TimeSlotResponse>> list(HttpServletRequest request,
                                                    @PathVariable UUID scheduleId) {
        AuthenticatedUser doctor = currentDoctorResolver.resolve(request);
        return apiResponseFactory.success("Time slots loaded", timeSlotService.list(doctor.userId(), scheduleId));
    }
}
