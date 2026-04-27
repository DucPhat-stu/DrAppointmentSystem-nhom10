package com.healthcare.doctor.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.doctor.config.AvailableSlotCacheProperties;
import com.healthcare.doctor.dto.AvailableSlotResponse;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AvailableSlotCache {
    private static final TypeReference<List<AvailableSlotResponse>> SLOT_LIST_TYPE = new TypeReference<>() {
    };

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final AvailableSlotCacheProperties properties;

    public AvailableSlotCache(StringRedisTemplate redisTemplate,
                              ObjectMapper objectMapper,
                              AvailableSlotCacheProperties properties) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public Optional<List<AvailableSlotResponse>> get(UUID doctorId, LocalDate date) {
        try {
            String raw = redisTemplate.opsForValue().get(key(doctorId, date));
            if (raw == null || raw.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(raw, SLOT_LIST_TYPE));
        } catch (RedisConnectionFailureException exception) {
            return Optional.empty();
        } catch (Exception exception) {
            evict(doctorId, date);
            return Optional.empty();
        }
    }

    public void put(UUID doctorId, LocalDate date, List<AvailableSlotResponse> slots) {
        try {
            redisTemplate.opsForValue().set(
                    key(doctorId, date),
                    objectMapper.writeValueAsString(slots),
                    Duration.ofSeconds(Math.max(properties.getTtlSeconds(), 1))
            );
        } catch (Exception ignored) {
            // Cache failures must not break slot availability reads.
        }
    }

    public void evict(UUID doctorId, LocalDate date) {
        try {
            redisTemplate.delete(key(doctorId, date));
        } catch (Exception ignored) {
            // Cache invalidation is best-effort; TTL is the final fallback.
        }
    }

    private String key(UUID doctorId, LocalDate date) {
        return "doctor:%s:slots:%s".formatted(doctorId, date);
    }
}
