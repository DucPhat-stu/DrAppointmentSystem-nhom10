package com.healthcare.appointment.client;

import com.healthcare.appointment.config.DoctorClientProperties;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.UUID;

@Component
public class RestDoctorSlotClient implements DoctorSlotClient {
    private static final int MAX_ATTEMPTS = 3;

    private final RestClient doctorRestClient;
    private final DoctorClientProperties properties;

    public RestDoctorSlotClient(RestClient doctorRestClient, DoctorClientProperties properties) {
        this.doctorRestClient = doctorRestClient;
        this.properties = properties;
    }

    @Override
    public void updateSlotStatus(UUID slotId, String status) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                doctorRestClient.put()
                        .uri("/internal/time-slots/{slotId}/status", slotId)
                        .header("X-Internal-Service-Token", properties.getServiceToken())
                        .body(Map.of("status", status))
                        .retrieve()
                        .toBodilessEntity();
                return;
            } catch (RestClientException exception) {
                if (attempt == MAX_ATTEMPTS) {
                    throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE, "Doctor slot service is unavailable");
                }
                backoff(attempt);
            }
        }
    }

    private void backoff(int attempt) {
        try {
            Thread.sleep(100L * attempt);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE, "Doctor slot service retry was interrupted");
        }
    }
}
