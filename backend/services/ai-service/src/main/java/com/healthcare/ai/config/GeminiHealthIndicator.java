package com.healthcare.ai.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class GeminiHealthIndicator implements HealthIndicator {
    private final GeminiProperties geminiProperties;
    private final RestClient restClient;

    public GeminiHealthIndicator(GeminiProperties geminiProperties, AIClientProperties clientProperties) {
        this.geminiProperties = geminiProperties;
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory(clientProperties.getTimeoutMs()))
                .build();
    }

    @Override
    public Health health() {
        if (geminiProperties.getApiKey() == null || geminiProperties.getApiKey().isBlank()) {
            return Health.down()
                    .withDetail("configured", false)
                    .withDetail("reason", "AI_API_KEY is not configured")
                    .build();
        }

        try {
            restClient.get()
                    .uri(modelsUrl())
                    .retrieve()
                    .toBodilessEntity();

            return Health.up()
                    .withDetail("configured", true)
                    .withDetail("provider", "gemini")
                    .withDetail("model", geminiProperties.getModel())
                    .build();
        } catch (Exception exception) {
            return Health.down()
                    .withDetail("configured", true)
                    .withDetail("provider", "gemini")
                    .withDetail("error", exception.getClass().getSimpleName())
                    .build();
        }
    }

    private String modelsUrl() {
        String apiKey = URLEncoder.encode(geminiProperties.getApiKey(), StandardCharsets.UTF_8);
        return "%s/models?key=%s".formatted(geminiProperties.getBaseUrl(), apiKey);
    }

    private static SimpleClientHttpRequestFactory requestFactory(int timeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        return factory;
    }
}
