package com.healthcare.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.ai.dto.AICheckResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AIResponseParser {
    private static final AICheckResponse FALLBACK = new AICheckResponse(
            List.of("Khong xac dinh"),
            List.of("Khong the phan tich"),
            "Noi tong quat",
            "Vui long mo ta ro hon hoac lien he bac si neu trieu chung keo dai."
    );

    private final ObjectMapper objectMapper;

    public AIResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AICheckResponse parse(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(extractJson(rawResponse));
            List<String> conditions = readStringArray(root, "possible_conditions", 5);
            List<String> symptoms = readStringArray(root, "symptoms_detected", 10);
            String specialty = normalize(root.path("recommended_specialty").asText(null), 100);
            String advice = normalize(root.path("advice").asText(null), 500);

            if (conditions.isEmpty() || symptoms.isEmpty() || specialty.isBlank() || advice.length() < 10) {
                return fallback();
            }

            return new AICheckResponse(conditions, symptoms, specialty, advice);
        } catch (Exception exception) {
            return fallback();
        }
    }

    public AICheckResponse fallback() {
        return FALLBACK;
    }

    private static String extractJson(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return "{}";
        }

        String trimmed = rawResponse.trim();
        if (trimmed.startsWith("```")) {
            int firstBrace = trimmed.indexOf('{');
            int lastBrace = trimmed.lastIndexOf('}');
            if (firstBrace >= 0 && lastBrace > firstBrace) {
                return trimmed.substring(firstBrace, lastBrace + 1);
            }
        }

        int firstBrace = trimmed.indexOf('{');
        int lastBrace = trimmed.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1);
        }

        return trimmed;
    }

    private static List<String> readStringArray(JsonNode root, String fieldName, int maxItems) {
        JsonNode node = root.path(fieldName);
        if (!node.isArray()) {
            return List.of();
        }

        List<String> values = new ArrayList<>();
        for (JsonNode item : node) {
            String value = normalize(item.asText(null), 100);
            if (!value.isBlank()) {
                values.add(value);
            }
            if (values.size() == maxItems) {
                break;
            }
        }
        return List.copyOf(values);
    }

    private static String normalize(String value, int maxLength) {
        if (value == null) {
            return "";
        }

        String normalized = value.trim().replaceAll("\\s+", " ");
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength).trim();
    }
}
