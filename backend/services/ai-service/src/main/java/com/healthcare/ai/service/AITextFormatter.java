package com.healthcare.ai.service;

import com.healthcare.ai.dto.AICheckResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AITextFormatter {
    private static final String UNKNOWN = "Khong xac dinh";
    private static final int MAX_SECTION_LENGTH = 500;

    public String format(AICheckResponse response) {
        AICheckResponse safe = response == null
                ? new AICheckResponse(List.of(), List.of(), UNKNOWN, UNKNOWN)
                : response;

        return """
                Cac benh co the: %s

                Trieu chung ghi nhan: %s

                Goi y kham: %s

                Loi khuyen: %s

                Luu y: Day chi la goi y tu AI, khong thay the chan doan y khoa. Hay lien he bac si neu trieu chung nang hoac keo dai.
                """.formatted(
                joinOrUnknown(safe.possibleConditions()),
                joinOrUnknown(safe.symptomsDetected()),
                textOrUnknown(safe.recommendedSpecialty()),
                truncate(textOrUnknown(safe.advice()), MAX_SECTION_LENGTH)
        ).trim();
    }

    private static String joinOrUnknown(List<String> values) {
        if (values == null || values.isEmpty()) {
            return UNKNOWN;
        }

        String joined = values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .reduce((left, right) -> left + ", " + right)
                .orElse(UNKNOWN);

        return truncate(joined, MAX_SECTION_LENGTH);
    }

    private static String textOrUnknown(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }
        return value.trim();
    }

    private static String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength - 3)).trim() + "...";
    }
}
