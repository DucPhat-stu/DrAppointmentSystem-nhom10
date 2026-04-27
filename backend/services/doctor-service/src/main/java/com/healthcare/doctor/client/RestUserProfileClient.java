package com.healthcare.doctor.client;

import com.healthcare.doctor.config.UserClientProperties;
import com.healthcare.doctor.dto.DoctorProfileSummaryResponse;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RestUserProfileClient implements UserProfileClient {
    private static final int MAX_ATTEMPTS = 3;
    private static final ParameterizedTypeReference<ApiResponse<List<DoctorProfileSummaryResponse>>> PROFILES_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient userRestClient;
    private final UserClientProperties properties;

    public RestUserProfileClient(RestClient userRestClient, UserClientProperties properties) {
        this.userRestClient = userRestClient;
        this.properties = properties;
    }

    @Override
    public List<DoctorProfileSummaryResponse> findProfiles(Collection<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        String ids = userIds.stream()
                .map(UUID::toString)
                .distinct()
                .collect(Collectors.joining(","));

        return handle(() -> userRestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/internal/users/profiles")
                        .queryParam("userIds", ids)
                        .build())
                .header("X-Internal-Service-Token", properties.getServiceToken())
                .retrieve()
                .body(PROFILES_TYPE)
                .data());
    }

    private <T> T handle(RemoteCall<T> call) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return call.execute();
            } catch (RestClientResponseException exception) {
                throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE, "User service returned an error");
            } catch (RestClientException exception) {
                if (attempt == MAX_ATTEMPTS) {
                    throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE, "User service is unavailable");
                }
                backoff(attempt);
            }
        }
        throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE, "User service is unavailable");
    }

    private void backoff(int attempt) {
        try {
            Thread.sleep(100L * attempt);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE, "User service retry was interrupted");
        }
    }

    private interface RemoteCall<T> {
        T execute();
    }
}
