package com.healthcare.appointment.web;

import com.healthcare.appointment.domain.AppointmentStatus;
import com.healthcare.appointment.dto.AppointmentActionRequest;
import com.healthcare.appointment.dto.AppointmentOwnershipResponse;
import com.healthcare.appointment.dto.AppointmentPageResponse;
import com.healthcare.appointment.dto.AppointmentResponse;
import com.healthcare.appointment.service.AppointmentService;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.web.ApiResponseFactory;
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
@RequestMapping("/internal/doctors/{doctorId}/appointments")
public class InternalDoctorAppointmentController {
    private final AppointmentService appointmentService;
    private final ApiResponseFactory apiResponseFactory;

    public InternalDoctorAppointmentController(AppointmentService appointmentService,
                                               ApiResponseFactory apiResponseFactory) {
        this.appointmentService = appointmentService;
        this.apiResponseFactory = apiResponseFactory;
    }

    @GetMapping
    public ApiResponse<AppointmentPageResponse> list(
            @PathVariable UUID doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return apiResponseFactory.success(
                "Appointments loaded",
                appointmentService.findDoctorAppointments(doctorId, date, status, page, size)
        );
    }

    @GetMapping("/{appointmentId}")
    public ApiResponse<AppointmentResponse> get(@PathVariable UUID doctorId,
                                                @PathVariable UUID appointmentId) {
        return apiResponseFactory.success(
                "Appointment loaded",
                appointmentService.getDoctorAppointment(doctorId, appointmentId)
        );
    }

    @GetMapping("/patients/{patientId}")
    public ApiResponse<AppointmentPageResponse> patientHistory(
            @PathVariable UUID doctorId,
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return apiResponseFactory.success(
                "Patient appointment history loaded",
                appointmentService.findDoctorPatientAppointments(doctorId, patientId, page, size)
        );
    }

    @GetMapping("/{appointmentId}/ownership")
    public ApiResponse<AppointmentOwnershipResponse> ownership(@PathVariable UUID doctorId,
                                                               @PathVariable UUID appointmentId) {
        return apiResponseFactory.success(
                "Appointment ownership loaded",
                appointmentService.getDoctorAppointmentOwnership(doctorId, appointmentId)
        );
    }

    @PutMapping("/{appointmentId}/confirm")
    public ApiResponse<AppointmentResponse> confirm(@PathVariable UUID doctorId,
                                                    @PathVariable UUID appointmentId,
                                                    @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {
        return apiResponseFactory.success(
                "Appointment confirmed",
                appointmentService.confirm(doctorId, appointmentId, idempotencyKey)
        );
    }

    @PutMapping("/{appointmentId}/reject")
    public ApiResponse<AppointmentResponse> reject(@PathVariable UUID doctorId,
                                                   @PathVariable UUID appointmentId,
                                                   @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
                                                   @Valid @RequestBody AppointmentActionRequest body) {
        return apiResponseFactory.success(
                "Appointment rejected",
                appointmentService.reject(doctorId, appointmentId, idempotencyKey, body)
        );
    }

    @PutMapping("/{appointmentId}/cancel")
    public ApiResponse<AppointmentResponse> cancel(@PathVariable UUID doctorId,
                                                   @PathVariable UUID appointmentId,
                                                   @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
                                                   @Valid @RequestBody AppointmentActionRequest body) {
        return apiResponseFactory.success(
                "Appointment cancelled",
                appointmentService.cancel(doctorId, appointmentId, idempotencyKey, body)
        );
    }

    @PutMapping("/{appointmentId}/complete")
    public ApiResponse<AppointmentResponse> complete(@PathVariable UUID doctorId,
                                                     @PathVariable UUID appointmentId,
                                                     @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {
        return apiResponseFactory.success(
                "Appointment completed",
                appointmentService.complete(doctorId, appointmentId, idempotencyKey)
        );
    }
}
