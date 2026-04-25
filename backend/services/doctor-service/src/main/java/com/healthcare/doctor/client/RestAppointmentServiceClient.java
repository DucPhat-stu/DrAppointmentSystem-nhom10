package com.healthcare.doctor.client;

import com.healthcare.doctor.dto.AppointmentActionRequest;
import com.healthcare.doctor.dto.DoctorAppointmentPageResponse;
import com.healthcare.doctor.dto.DoctorAppointmentResponse;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class RestAppointmentServiceClient implements AppointmentServiceClient {
    private static final ParameterizedTypeReference<ApiResponse<DoctorAppointmentPageResponse>> PAGE_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<ApiResponse<DoctorAppointmentResponse>> DETAIL_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient appointmentRestClient;

    public RestAppointmentServiceClient(RestClient appointmentRestClient) {
        this.appointmentRestClient = appointmentRestClient;
    }

    @Override
    public DoctorAppointmentPageResponse findDoctorAppointments(UUID doctorId, LocalDate date, String status, int page, int size) {
        return handle(() -> appointmentRestClient.get()
                .uri(builder -> {
                    var uriBuilder = builder.path("/internal/doctors/{doctorId}/appointments")
                            .queryParam("page", page)
                            .queryParam("size", size);
                    if (date != null) {
                        uriBuilder.queryParam("date", date);
                    }
                    if (status != null && !status.isBlank()) {
                        uriBuilder.queryParam("status", status);
                    }
                    return uriBuilder.build(doctorId);
                })
                .retrieve()
                .body(PAGE_TYPE)
                .data());
    }

    @Override
    public DoctorAppointmentResponse getDoctorAppointment(UUID doctorId, UUID appointmentId) {
        return handle(() -> appointmentRestClient.get()
                .uri("/internal/doctors/{doctorId}/appointments/{appointmentId}", doctorId, appointmentId)
                .retrieve()
                .body(DETAIL_TYPE)
                .data());
    }

    @Override
    public DoctorAppointmentResponse confirm(UUID doctorId, UUID appointmentId, String idempotencyKey) {
        return action(doctorId, appointmentId, "confirm", idempotencyKey, null);
    }

    @Override
    public DoctorAppointmentResponse reject(UUID doctorId, UUID appointmentId, String idempotencyKey, AppointmentActionRequest request) {
        return action(doctorId, appointmentId, "reject", idempotencyKey, request);
    }

    @Override
    public DoctorAppointmentResponse cancel(UUID doctorId, UUID appointmentId, String idempotencyKey, AppointmentActionRequest request) {
        return action(doctorId, appointmentId, "cancel", idempotencyKey, request);
    }

    private DoctorAppointmentResponse action(UUID doctorId,
                                             UUID appointmentId,
                                             String action,
                                             String idempotencyKey,
                                             AppointmentActionRequest request) {
        return handle(() -> {
            RestClient.RequestBodySpec spec = appointmentRestClient.put()
                    .uri("/internal/doctors/{doctorId}/appointments/{appointmentId}/{action}", doctorId, appointmentId, action);
            if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                spec.header("X-Idempotency-Key", idempotencyKey);
            }
            return spec.body(request != null ? request : new AppointmentActionRequest(null))
                    .retrieve()
                    .body(DETAIL_TYPE)
                    .data();
        });
    }

    private <T> T handle(RemoteCall<T> call) {
        try {
            return call.execute();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Appointment not found");
            }
            if (exception.getStatusCode() == HttpStatus.CONFLICT) {
                throw new ApiException(ErrorCode.CONFLICT, "Appointment action is not allowed");
            }
            throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE, "Appointment service returned an error");
        } catch (RestClientException exception) {
            throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE, "Appointment service is unavailable");
        }
    }

    private interface RemoteCall<T> {
        T execute();
    }
}
