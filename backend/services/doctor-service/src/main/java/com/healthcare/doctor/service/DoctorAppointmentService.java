package com.healthcare.doctor.service;

import com.healthcare.doctor.client.AppointmentServiceClient;
import com.healthcare.doctor.dto.AppointmentActionRequest;
import com.healthcare.doctor.dto.DoctorAppointmentPageResponse;
import com.healthcare.doctor.dto.DoctorAppointmentOwnershipResponse;
import com.healthcare.doctor.dto.DoctorAppointmentResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class DoctorAppointmentService {
    private final AppointmentServiceClient appointmentServiceClient;

    public DoctorAppointmentService(AppointmentServiceClient appointmentServiceClient) {
        this.appointmentServiceClient = appointmentServiceClient;
    }

    public DoctorAppointmentPageResponse find(UUID doctorId, LocalDate date, String status, int page, int size) {
        return appointmentServiceClient.findDoctorAppointments(doctorId, date, status, page, size);
    }

    public DoctorAppointmentResponse get(UUID doctorId, UUID appointmentId) {
        return appointmentServiceClient.getDoctorAppointment(doctorId, appointmentId);
    }

    public DoctorAppointmentOwnershipResponse ownership(UUID doctorId, UUID appointmentId) {
        return appointmentServiceClient.getDoctorAppointmentOwnership(doctorId, appointmentId);
    }

    public DoctorAppointmentResponse confirm(UUID doctorId, UUID appointmentId, String idempotencyKey) {
        return appointmentServiceClient.confirm(doctorId, appointmentId, idempotencyKey);
    }

    public DoctorAppointmentResponse reject(UUID doctorId, UUID appointmentId, String idempotencyKey, AppointmentActionRequest request) {
        return appointmentServiceClient.reject(doctorId, appointmentId, idempotencyKey, request);
    }

    public DoctorAppointmentResponse cancel(UUID doctorId, UUID appointmentId, String idempotencyKey, AppointmentActionRequest request) {
        return appointmentServiceClient.cancel(doctorId, appointmentId, idempotencyKey, request);
    }

    public DoctorAppointmentResponse complete(UUID doctorId, UUID appointmentId, String idempotencyKey) {
        return appointmentServiceClient.complete(doctorId, appointmentId, idempotencyKey);
    }
}
