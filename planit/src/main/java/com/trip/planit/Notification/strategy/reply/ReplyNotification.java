package com.trip.planit.Notification.strategy.reply;

import com.trip.planit.Notification.constants.NotificationConstants;
import com.trip.planit.Notification.exception.NotificationException;
import com.trip.planit.Notification.model.Notification;
import com.trip.planit.Notification.strategy.NotificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
@RequiredArgsConstructor
public class ReplyNotification implements NotificationSender {
    private final RedisTemplate<String, Notification> redisTemplate;

    @Override
    public void send(Notification notification) {
        try {
            redisTemplate.convertAndSend(NotificationConstants.CHANNEL_REPLY, notification);
            System.out.println("reply notification : " + notification.getMessage());
        } catch (Exception e) {
            throw new NotificationException("Failed to send reply notification", e);
        }
    }
}
