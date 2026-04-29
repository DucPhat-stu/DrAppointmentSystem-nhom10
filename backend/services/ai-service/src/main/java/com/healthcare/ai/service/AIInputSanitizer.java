package com.healthcare.ai.service;

import org.springframework.stereotype.Component;

@Component
public class AIInputSanitizer {
    public String sanitize(String value, int maxLength) {
        if (value == null) {
            return "";
        }

        String sanitized = value
                .replaceAll("<[^>]*>", " ")
                .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (sanitized.length() <= maxLength) {
            return sanitized;
        }

        return sanitized.substring(0, maxLength).trim();
    }
}
