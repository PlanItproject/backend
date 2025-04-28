package com.trip.planit.Notification.strategy.chat;

import static com.trip.planit.Notification.constants.NotificationConstants.STREAM_KEY;

import com.trip.planit.Notification.model.Notification;
import com.trip.planit.Notification.strategy.NotificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RedisStreamNotificationSender implements NotificationSender {
    private final RedisTemplate<String, Notification> redisNotificationTemplate;

    @Override
    public void send(Notification notification) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("message", notification.getMessage());
        fields.put("type", notification.getType().name());
        fields.put("createdAt", notification.getCreatedAt().toString());
        fields.put("isRead", notification.isRead());
        fields.put("targetToken", notification.getTargetToken());

        redisNotificationTemplate.opsForStream().add(STREAM_KEY, fields);
        System.out.println("✅ Redis Stream에 채팅 알림 전송 완료: " + notification.getMessage());
    }
}
