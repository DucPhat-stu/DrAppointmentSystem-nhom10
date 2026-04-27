package com.healthcare.appointment.web;

import com.healthcare.appointment.dto.AppointmentResponse;
import com.healthcare.appointment.dto.CreateAppointmentRequest;
import com.healthcare.appointment.security.CurrentPatientResolver;
import com.healthcare.appointment.service.AppointmentService;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.web.ApiResponseFactory;
import com.healthcare.shared.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping("/{id}")
    public ApiResponse<AppointmentResponse> get(HttpServletRequest request, @PathVariable UUID id) {
        AuthenticatedUser patient = currentPatientResolver.resolve(request);
        return apiResponseFactory.success("Appointment loaded", appointmentService.getPatientAppointment(patient.userId(), id));
    }
}
