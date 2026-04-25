package com.healthcare.doctor.web;

import com.healthcare.doctor.dto.SoapNoteRequest;
import com.healthcare.doctor.dto.SoapNoteResponse;
import com.healthcare.doctor.security.CurrentDoctorResolver;
import com.healthcare.doctor.service.SoapNoteService;
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
@RequestMapping("/api/v1/doctors/appointments/{appointmentId}/soap")
public class SoapNoteController {
    private final SoapNoteService soapNoteService;
    private final CurrentDoctorResolver currentDoctorResolver;
    private final ApiResponseFactory apiResponseFactory;

    public SoapNoteController(SoapNoteService soapNoteService,
                              CurrentDoctorResolver currentDoctorResolver,
                              ApiResponseFactory apiResponseFactory) {
        this.soapNoteService = soapNoteService;
        this.currentDoctorResolver = currentDoctorResolver;
        this.apiResponseFactory = apiResponseFactory;
    }

    @PostMapping
    public ApiResponse<SoapNoteResponse> save(HttpServletRequest request,
                                              @PathVariable UUID appointmentId,
                                              @Valid @RequestBody SoapNoteRequest body) {
        AuthenticatedUser doctor = currentDoctorResolver.resolve(request);
        return apiResponseFactory.success("SOAP note saved", soapNoteService.save(doctor.userId(), appointmentId, body));
    }

    @GetMapping
    public ApiResponse<SoapNoteResponse> get(HttpServletRequest request, @PathVariable UUID appointmentId) {
        AuthenticatedUser doctor = currentDoctorResolver.resolve(request);
        return apiResponseFactory.success("SOAP note loaded", soapNoteService.get(doctor.userId(), appointmentId));
    }
}
