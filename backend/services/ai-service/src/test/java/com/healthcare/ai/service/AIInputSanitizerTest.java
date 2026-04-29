package com.healthcare.ai.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AIInputSanitizerTest {
    private final AIInputSanitizer sanitizer = new AIInputSanitizer();

    @Test
    void removesBasicHtmlTagsAndNormalizesSpaces() {
        String value = sanitizer.sanitize("  <script>alert(1)</script>   ho   sot  ", 80);

        assertThat(value).doesNotContain("<script>");
        assertThat(value).isEqualTo("alert(1) ho sot");
    }

    @Test
    void truncatesLongText() {
        String value = sanitizer.sanitize("abcdef", 3);

        assertThat(value).isEqualTo("abc");
    }
}
