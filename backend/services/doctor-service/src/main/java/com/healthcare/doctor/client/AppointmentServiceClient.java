package com.healthcare.doctor.client;

import com.healthcare.doctor.dto.AppointmentActionRequest;
import com.healthcare.doctor.dto.DoctorAppointmentPageResponse;
import com.healthcare.doctor.dto.DoctorAppointmentOwnershipResponse;
import com.healthcare.doctor.dto.DoctorAppointmentResponse;

import java.time.LocalDate;
import java.util.UUID;

public interface AppointmentServiceClient {
    DoctorAppointmentPageResponse findDoctorAppointments(UUID doctorId, LocalDate date, String status, int page, int size);

    DoctorAppointmentPageResponse findDoctorPatientAppointments(UUID doctorId, UUID patientId, int page, int size);

    DoctorAppointmentResponse getDoctorAppointment(UUID doctorId, UUID appointmentId);

    DoctorAppointmentOwnershipResponse getDoctorAppointmentOwnership(UUID doctorId, UUID appointmentId);

    DoctorAppointmentResponse confirm(UUID doctorId, UUID appointmentId, String idempotencyKey);

    DoctorAppointmentResponse reject(UUID doctorId, UUID appointmentId, String idempotencyKey, AppointmentActionRequest request);

    DoctorAppointmentResponse cancel(UUID doctorId, UUID appointmentId, String idempotencyKey, AppointmentActionRequest request);

    DoctorAppointmentResponse complete(UUID doctorId, UUID appointmentId, String idempotencyKey);
}
