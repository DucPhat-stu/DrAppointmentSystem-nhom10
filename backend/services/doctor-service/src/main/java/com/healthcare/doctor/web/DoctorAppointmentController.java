package com.healthcare.doctor.web;

import com.healthcare.doctor.dto.AppointmentActionRequest;
import com.healthcare.doctor.dto.DoctorAppointmentPageResponse;
import com.healthcare.doctor.dto.DoctorAppointmentResponse;
import com.healthcare.doctor.security.CurrentDoctorResolver;
import com.healthcare.doctor.service.DoctorAppointmentService;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.web.ApiResponseFactory;
import com.healthcare.shared.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctors/appointments")
public class DoctorAppointmentController {
    private final DoctorAppointmentService appointmentService;
    private final CurrentDoctorResolver currentDoctorResolver;
    private final ApiResponseFactory apiResponseFactory;

    public DoctorAppointmentController(DoctorAppointmentService appointmentService,
                                       CurrentDoctorResolver currentDoctorResolver,
                                       ApiResponseFactory apiResponseFactory) {
        this.appointmentService = appointmentService;
        this.currentDoctorResolver = currentDoctorResolver;
        this.apiResponseFactory = apiResponseFactory;
    }

    @GetMapping
    public ApiResponse<DoctorAppointmentPageResponse> list(
            HttpServletRequest request,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        AuthenticatedUser doctor = currentDoctorResolver.resolve(request);
        return apiResponseFactory.success(
                "Appointments loaded",
                appointmentService.find(doctor.userId(), date, status, page, size)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<DoctorAppointmentResponse> get(HttpServletRequest request, @PathVariable UUID id) {
        AuthenticatedUser doctor = currentDoctorResolver.resolve(request);
        return apiResponseFactory.success("Appointment loaded", appointmentService.get(doctor.userId(), id));
    }

    @PutMapping("/{id}/confirm")
    public ApiResponse<DoctorAppointmentResponse> confirm(
            HttpServletRequest request,
            @PathVariable UUID id,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {
        AuthenticatedUser doctor = currentDoctorResolver.resolve(request);
        return apiResponseFactory.success("Appointment confirmed", appointmentService.confirm(doctor.userId(), id, idempotencyKey));
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<DoctorAppointmentResponse> reject(
            HttpServletRequest request,
            @PathVariable UUID id,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody AppointmentActionRequest body) {
        AuthenticatedUser doctor = currentDoctorResolver.resolve(request);
        return apiResponseFactory.success("Appointment rejected", appointmentService.reject(doctor.userId(), id, idempotencyKey, body));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<DoctorAppointmentResponse> cancel(
            HttpServletRequest request,
            @PathVariable UUID id,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody AppointmentActionRequest body) {
        AuthenticatedUser doctor = currentDoctorResolver.resolve(request);
        return apiResponseFactory.success("Appointment cancelled", appointmentService.cancel(doctor.userId(), id, idempotencyKey, body));
    }

    @PutMapping("/{id}/complete")
    public ApiResponse<DoctorAppointmentResponse> complete(
            HttpServletRequest request,
            @PathVariable UUID id,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {
        AuthenticatedUser doctor = currentDoctorResolver.resolve(request);
        return apiResponseFactory.success("Appointment completed", appointmentService.complete(doctor.userId(), id, idempotencyKey));
    }
}
