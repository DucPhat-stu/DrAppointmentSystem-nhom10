package com.healthcare.doctor.client;

import com.healthcare.doctor.dto.AppointmentActionRequest;
import com.healthcare.doctor.config.AppointmentClientProperties;
import com.healthcare.doctor.dto.DoctorAppointmentPageResponse;
import com.healthcare.doctor.dto.DoctorAppointmentOwnershipResponse;
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
    private static final int MAX_ATTEMPTS = 3;
    private static final ParameterizedTypeReference<ApiResponse<DoctorAppointmentPageResponse>> PAGE_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<ApiResponse<DoctorAppointmentResponse>> DETAIL_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<ApiResponse<DoctorAppointmentOwnershipResponse>> OWNERSHIP_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient appointmentRestClient;
    private final AppointmentClientProperties properties;

    public RestAppointmentServiceClient(RestClient appointmentRestClient, AppointmentClientProperties properties) {
        this.appointmentRestClient = appointmentRestClient;
        this.properties = properties;
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
                .header("X-Internal-Service-Token", properties.getServiceToken())
                .retrieve()
                .body(PAGE_TYPE)
                .data());
    }

    @Override
    public DoctorAppointmentResponse getDoctorAppointment(UUID doctorId, UUID appointmentId) {
        return handle(() -> appointmentRestClient.get()
                .uri("/internal/doctors/{doctorId}/appointments/{appointmentId}", doctorId, appointmentId)
                .header("X-Internal-Service-Token", properties.getServiceToken())
                .retrieve()
                .body(DETAIL_TYPE)
                .data());
    }

    @Override
    public DoctorAppointmentOwnershipResponse getDoctorAppointmentOwnership(UUID doctorId, UUID appointmentId) {
        return handle(() -> appointmentRestClient.get()
                .uri("/internal/doctors/{doctorId}/appointments/{appointmentId}/ownership", doctorId, appointmentId)
                .header("X-Internal-Service-Token", properties.getServiceToken())
                .retrieve()
                .body(OWNERSHIP_TYPE)
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
            spec.header("X-Internal-Service-Token", properties.getServiceToken());
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
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
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
                if (attempt == MAX_ATTEMPTS) {
                    throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE, "Appointment service is unavailable");
                }
                backoff(attempt);
            }
        }
        throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE, "Appointment service is unavailable");
    }

    private void backoff(int attempt) {
        try {
            Thread.sleep(100L * attempt);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE, "Appointment service retry was interrupted");
        }
    }

    private interface RemoteCall<T> {
        T execute();
    }
}
