package com.healthcare.ai.service;

import com.healthcare.ai.dto.AICheckResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AITextFormatterTest {
    private final AITextFormatter formatter = new AITextFormatter();

    @Test
    void formatIncludesAllSectionsAndDisclaimer() {
        AICheckResponse response = new AICheckResponse(
                List.of("Cam cum"),
                List.of("Ho", "Sot"),
                "Noi tong quat",
                "Nghi ngoi, uong du nuoc va theo doi them."
        );

        String formatted = formatter.format(response);

        assertThat(formatted).contains("Cac benh co the: Cam cum");
        assertThat(formatted).contains("Trieu chung ghi nhan: Ho, Sot");
        assertThat(formatted).contains("Goi y kham: Noi tong quat");
        assertThat(formatted).contains("Luu y:");
    }

    @Test
    void formatHandlesNullAndEmptyValues() {
        AICheckResponse response = new AICheckResponse(List.of(), null, null, "");

        String formatted = formatter.format(response);

        assertThat(formatted).doesNotContain("null");
        assertThat(formatted).contains("Khong xac dinh");
    }

    @Test
    void formatTruncatesLongAdvice() {
        String advice = "a".repeat(700);
        AICheckResponse response = new AICheckResponse(List.of("A"), List.of("B"), "C", advice);

        String formatted = formatter.format(response);

        assertThat(formatted).contains("...");
        assertThat(formatted.length()).isLessThan(900);
    }
}
