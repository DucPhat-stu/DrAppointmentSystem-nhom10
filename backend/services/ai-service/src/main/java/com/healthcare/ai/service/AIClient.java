package com.healthcare.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.ai.config.AIClientProperties;
import com.healthcare.ai.config.GeminiProperties;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class AIClient {
    private static final String FALLBACK_JSON = """
            {
              "possible_conditions": ["Khong xac dinh"],
              "symptoms_detected": ["Khong the phan tich"],
              "recommended_specialty": "Noi tong quat",
              "advice": "He thong AI tam thoi khong san sang. Vui long thu lai hoac lien he bac si neu trieu chung keo dai."
            }
            """;

    private final GeminiProperties geminiProperties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public AIClient(GeminiProperties geminiProperties,
                    AIClientProperties clientProperties,
                    ObjectMapper objectMapper) {
        this.geminiProperties = geminiProperties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory(clientProperties.getTimeoutMs()))
                .build();
    }

    public String generate(String prompt) {
        if (geminiProperties.getApiKey() == null || geminiProperties.getApiKey().isBlank()) {
            return FALLBACK_JSON;
        }

        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                String body = restClient.post()
                        .uri(geminiUrl())
                        .body(requestBody(prompt))
                        .retrieve()
                        .body(String.class);

                return extractText(body);
            } catch (RestClientException | IllegalArgumentException exception) {
                if (attempt == 1) {
                    return FALLBACK_JSON;
                }
            }
        }

        return FALLBACK_JSON;
    }

    private static SimpleClientHttpRequestFactory requestFactory(int timeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        return factory;
    }

    private String geminiUrl() {
        String model = URLEncoder.encode(geminiProperties.getModel(), StandardCharsets.UTF_8);
        String apiKey = URLEncoder.encode(geminiProperties.getApiKey(), StandardCharsets.UTF_8);
        return "%s/models/%s:generateContent?key=%s".formatted(
                geminiProperties.getBaseUrl(),
                model,
                apiKey
        );
    }

    private static Map<String, Object> requestBody(String prompt) {
        return Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                ))
        );
    }

    private String extractText(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            if (text.isTextual() && !text.asText().isBlank()) {
                return text.asText();
            }
        } catch (Exception ignored) {
            return body;
        }

        return body;
    }
}
