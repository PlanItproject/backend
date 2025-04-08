package com.trip.planit.Notification.util;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisDuplicateChecker {
    private final RedisTemplate<String, String> redisStringTemplate;

    public boolean isDuplicate(String key) {
        return Boolean.TRUE.equals(redisStringTemplate.hasKey(key));
    }

    public void markAsSent(String key, Duration ttl) {
        redisStringTemplate.opsForValue().set(key, "sent", ttl);
    }
}