package com.healthcare.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.rate-limit")
public class RateLimitProperties {
    private boolean enabled = true;
    private int perUserPerMinute = 10;
    private int globalPerMinute = 100;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPerUserPerMinute() {
        return perUserPerMinute;
    }

    public void setPerUserPerMinute(int perUserPerMinute) {
        this.perUserPerMinute = perUserPerMinute;
    }

    public int getGlobalPerMinute() {
        return globalPerMinute;
    }

    public void setGlobalPerMinute(int globalPerMinute) {
        this.globalPerMinute = globalPerMinute;
    }
}
