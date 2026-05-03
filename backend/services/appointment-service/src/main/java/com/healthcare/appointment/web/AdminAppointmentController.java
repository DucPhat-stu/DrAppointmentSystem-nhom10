package com.healthcare.appointment.web;

import com.healthcare.appointment.dto.AppointmentActionRequest;
import com.healthcare.appointment.dto.AppointmentPageResponse;
import com.healthcare.appointment.dto.AppointmentResponse;
import com.healthcare.appointment.security.CurrentAdminResolver;
import com.healthcare.appointment.service.AppointmentService;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.web.ApiResponseFactory;
import com.healthcare.shared.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Admin Appointment Governance.
 * GET /api/v1/admin/appointments       — list all appointments (filter by status + pagination)
 * PUT /api/v1/admin/appointments/{id}/cancel — force cancel, releases slot, fires event
 */
@RestController
@RequestMapping("/api/v1/admin/appointments")
public class AdminAppointmentController {

    private final AppointmentService appointmentService;
    private final CurrentAdminResolver currentAdminResolver;
    private final ApiResponseFactory apiResponseFactory;

    public AdminAppointmentController(AppointmentService appointmentService,
                                      CurrentAdminResolver currentAdminResolver,
                                      ApiResponseFactory apiResponseFactory) {
        this.appointmentService = appointmentService;
        this.currentAdminResolver = currentAdminResolver;
        this.apiResponseFactory = apiResponseFactory;
    }

    @GetMapping
    public ApiResponse<AppointmentPageResponse> listAll(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        currentAdminResolver.resolve(request);
        return apiResponseFactory.success(
                "Appointments loaded",
                appointmentService.findAllAppointments(status, page, size)
        );
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<AppointmentResponse> forceCancel(
            HttpServletRequest request,
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) AppointmentActionRequest body) {
        AuthenticatedUser admin = currentAdminResolver.resolve(request);
        String reason = body != null ? body.reason() : null;
        return apiResponseFactory.success(
                "Appointment cancelled by admin",
                appointmentService.adminCancelAppointment(admin.userId(), id, reason)
        );
    }
}
