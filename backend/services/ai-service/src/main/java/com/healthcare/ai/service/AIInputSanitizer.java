package com.healthcare.ai.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class AIInputSanitizer {
    private static final String REMOVED_INSTRUCTION = "[removed unsafe instruction]";
    private static final List<Pattern> PROMPT_INJECTION_PATTERNS = List.of(
            Pattern.compile("(?i)\\b(ignore|disregard|forget|bypass)\\s+(all\\s+)?(previous|prior|above|system|developer)\\s+(instructions?|prompts?|rules?)\\b"),
            Pattern.compile("(?i)\\b(you are now|act as|pretend to be)\\s+[^.!?]{0,80}"),
            Pattern.compile("(?i)\\b(reveal|show|print|leak|expose)\\s+(the\\s+)?(system\\s+)?(prompt|instructions?|rules?|developer message)\\b"),
            Pattern.compile("(?i)\\bdo\\s+not\\s+(follow|obey)\\s+(the\\s+)?(system|developer|previous)\\s+(instructions?|rules?)\\b")
    );

    public String sanitize(String value, int maxLength) {
        if (value == null) {
            return "";
        }

        String sanitized = value
                .replaceAll("<[^>]*>", " ")
                .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        for (Pattern pattern : PROMPT_INJECTION_PATTERNS) {
            sanitized = pattern.matcher(sanitized).replaceAll(REMOVED_INSTRUCTION);
        }

        sanitized = sanitized.replaceAll("\\s+", " ").trim();

        if (sanitized.length() <= maxLength) {
            return sanitized;
        }

        return sanitized.substring(0, maxLength).trim();
    }
}
