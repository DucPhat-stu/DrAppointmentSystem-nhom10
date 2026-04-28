package com.healthcare.appointment.web;

import com.healthcare.appointment.dto.AppointmentResponse;
import com.healthcare.appointment.dto.AppointmentActionRequest;
import com.healthcare.appointment.dto.AppointmentPageResponse;
import com.healthcare.appointment.dto.CreateAppointmentRequest;
import com.healthcare.appointment.dto.RescheduleAppointmentRequest;
import com.healthcare.appointment.security.CurrentPatientResolver;
import com.healthcare.appointment.service.AppointmentService;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.web.ApiResponseFactory;
import com.healthcare.shared.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final CurrentPatientResolver currentPatientResolver;
    private final ApiResponseFactory apiResponseFactory;

    public AppointmentController(AppointmentService appointmentService,
                                 CurrentPatientResolver currentPatientResolver,
                                 ApiResponseFactory apiResponseFactory) {
        this.appointmentService = appointmentService;
        this.currentPatientResolver = currentPatientResolver;
        this.apiResponseFactory = apiResponseFactory;
    }

    @PostMapping
    public ApiResponse<AppointmentResponse> create(HttpServletRequest request,
                                                   @Valid @RequestBody CreateAppointmentRequest body) {
        AuthenticatedUser patient = currentPatientResolver.resolve(request);
        return apiResponseFactory.success("Appointment requested", appointmentService.create(patient.userId(), body));
    }

    @GetMapping
    public ApiResponse<AppointmentPageResponse> list(HttpServletRequest request,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size,
                                                     @RequestParam(required = false) String status) {
        AuthenticatedUser patient = currentPatientResolver.resolve(request);
        return apiResponseFactory.success(
                "Appointments loaded",
                appointmentService.findPatientAppointments(patient.userId(), status, page, size)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<AppointmentResponse> get(HttpServletRequest request, @PathVariable UUID id) {
        AuthenticatedUser patient = currentPatientResolver.resolve(request);
        return apiResponseFactory.success("Appointment loaded", appointmentService.getPatientAppointment(patient.userId(), id));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<AppointmentResponse> cancel(HttpServletRequest request,
                                                   @PathVariable UUID id,
                                                   @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
                                                   @Valid @RequestBody(required = false) AppointmentActionRequest body) {
        AuthenticatedUser patient = currentPatientResolver.resolve(request);
        AppointmentActionRequest action = body == null ? new AppointmentActionRequest(null) : body;
        return apiResponseFactory.success(
                "Appointment cancelled",
                appointmentService.cancelPatientAppointment(patient.userId(), id, idempotencyKey, action)
        );
    }

    @DeleteMapping("/{id}/cancel")
    public ApiResponse<AppointmentResponse> cancelWithDelete(HttpServletRequest request,
                                                             @PathVariable UUID id,
                                                             @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
                                                             @Valid @RequestBody(required = false) AppointmentActionRequest body) {
        return cancel(request, id, idempotencyKey, body);
    }

    @PutMapping("/{id}/reschedule")
    public ApiResponse<AppointmentResponse> reschedule(HttpServletRequest request,
                                                       @PathVariable UUID id,
                                                       @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
                                                       @Valid @RequestBody RescheduleAppointmentRequest body) {
        AuthenticatedUser patient = currentPatientResolver.resolve(request);
        return apiResponseFactory.success(
                "Appointment rescheduled",
                appointmentService.reschedulePatientAppointment(patient.userId(), id, idempotencyKey, body)
        );
    }
}
