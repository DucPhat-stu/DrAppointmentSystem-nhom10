package com.healthcare.shared.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.service")
public class AppServiceProperties {
    private String name;
    private String description;
    private String database;
    private boolean rabbitmqEnabled;
    private boolean redisEnabled;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public boolean isRabbitmqEnabled() {
        return rabbitmqEnabled;
    }

    public void setRabbitmqEnabled(boolean rabbitmqEnabled) {
        this.rabbitmqEnabled = rabbitmqEnabled;
    }

    public boolean isRedisEnabled() {
        return redisEnabled;
    }

    public void setRedisEnabled(boolean redisEnabled) {
        this.redisEnabled = redisEnabled;
    }
}

