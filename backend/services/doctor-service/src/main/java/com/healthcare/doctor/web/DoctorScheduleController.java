package com.healthcare.doctor.web;

import com.healthcare.doctor.dto.ScheduleRequest;
import com.healthcare.doctor.dto.ScheduleResponse;
import com.healthcare.doctor.security.CurrentDoctorResolver;
import com.healthcare.doctor.service.DoctorScheduleService;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.web.ApiResponseFactory;
import com.healthcare.shared.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctors/schedules")
public class DoctorScheduleController {
    private final DoctorScheduleService scheduleService;
    private final CurrentDoctorResolver currentDoctorResolver;
    private final ApiResponseFactory apiResponseFactory;

    public DoctorScheduleController(DoctorScheduleService scheduleService,
                                    CurrentDoctorResolver currentDoctorResolver,
                                    ApiResponseFactory apiResponseFactory) {
        this.scheduleService = scheduleService;
        this.currentDoctorResolver = currentDoctorResolver;
        this.apiResponseFactory = apiResponseFactory;
    }

    @PostMapping
    public ApiResponse<ScheduleResponse> create(HttpServletRequest request,
                                                @Valid @RequestBody ScheduleRequest body) {
        AuthenticatedUser doctor = currentDoctorResolver.resolve(request);
        ScheduleResponse schedule = scheduleService.create(doctor.userId(), body);
        return apiResponseFactory.success("Schedule created", schedule);
    }

    @GetMapping
    public ApiResponse<List<ScheduleResponse>> list(HttpServletRequest request) {
        AuthenticatedUser doctor = currentDoctorResolver.resolve(request);
        return apiResponseFactory.success("Schedules loaded", scheduleService.list(doctor.userId()));
    }

    @PutMapping("/{id}")
    public ApiResponse<ScheduleResponse> update(HttpServletRequest request,
                                                @PathVariable UUID id,
                                                @Valid @RequestBody ScheduleRequest body) {
        AuthenticatedUser doctor = currentDoctorResolver.resolve(request);
        ScheduleResponse schedule = scheduleService.update(doctor.userId(), id, body);
        return apiResponseFactory.success("Schedule updated", schedule);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(HttpServletRequest request, @PathVariable UUID id) {
        AuthenticatedUser doctor = currentDoctorResolver.resolve(request);
        scheduleService.delete(doctor.userId(), id);
        return apiResponseFactory.success("Schedule deleted", null);
    }
}
