package com.healthcare.appointment.client;

import com.healthcare.appointment.config.DoctorClientProperties;
import com.healthcare.appointment.dto.DoctorSlotResponse;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;
import java.util.UUID;

@Component
public class RestDoctorSlotClient implements DoctorSlotClient {
    private static final int MAX_ATTEMPTS = 3;
    private static final ParameterizedTypeReference<ApiResponse<DoctorSlotResponse>> SLOT_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient doctorRestClient;
    private final DoctorClientProperties properties;

    public RestDoctorSlotClient(RestClient doctorRestClient, DoctorClientProperties properties) {
        this.doctorRestClient = doctorRestClient;
        this.properties = properties;
    }

    @Override
    public DoctorSlotResponse getSlot(UUID slotId) {
        return handle(() -> doctorRestClient.get()
                .uri("/internal/time-slots/{slotId}", slotId)
                .header("X-Internal-Service-Token", properties.getServiceToken())
                .retrieve()
                .body(SLOT_TYPE)
                .data());
    }

    @Override
    public void updateSlotStatus(UUID slotId, String status) {
        handle(() -> {
            doctorRestClient.put()
                    .uri("/internal/time-slots/{slotId}/status", slotId)
                    .header("X-Internal-Service-Token", properties.getServiceToken())
                    .body(Map.of("status", status))
                    .retrieve()
                    .toBodilessEntity();
            return null;
        });
    }

    private <T> T handle(RemoteCall<T> call) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return call.execute();
            } catch (RestClientResponseException exception) {
                if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                    throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Doctor slot not found");
                }
                throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE, "Doctor slot service returned an error");
            } catch (RestClientException exception) {
                if (attempt == MAX_ATTEMPTS) {
                    throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE, "Doctor slot service is unavailable");
                }
                backoff(attempt);
            }
        }
        throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE, "Doctor slot service is unavailable");
    }

    private void backoff(int attempt) {
        try {
            Thread.sleep(100L * attempt);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE, "Doctor slot service retry was interrupted");
        }
    }

    private interface RemoteCall<T> {
        T execute();
    }
}
