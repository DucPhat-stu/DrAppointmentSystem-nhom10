package com.healthcare.doctor.service;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class SoapInputSanitizer {
    private static final Pattern SCRIPT_BLOCK = Pattern.compile("(?is)<script.*?>.*?</script>");

    public String sanitize(String value) {
        String withoutScripts = SCRIPT_BLOCK.matcher(value.trim()).replaceAll("");
        return withoutScripts
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
