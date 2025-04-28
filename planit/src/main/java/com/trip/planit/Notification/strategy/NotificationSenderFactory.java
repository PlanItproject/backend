package com.trip.planit.Notification.strategy;

import com.trip.planit.Notification.model.NotificationType;
import com.trip.planit.Notification.strategy.fcm.FcmNotificationSender;
import com.trip.planit.Notification.strategy.reply.ReplyNotification;
import com.trip.planit.Notification.strategy.chat.RedisStreamNotificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import com.google.firebase.messaging.FirebaseMessaging;
import com.trip.planit.Notification.model.Notification;

@Component
@RequiredArgsConstructor
public class NotificationSenderFactory {

    private final FirebaseMessaging firebaseMessaging;
    private final RedisTemplate<String, Notification> redisNotificationTemplate;
    private final RedisTemplate<String, Notification> redisTemplate;

    public NotificationSender createSender(NotificationType type) {
        switch (type) {
            case CHAT:
                return new CompositeNotificationSender(new NotificationSender[]{
                        new FcmNotificationSender(firebaseMessaging),
                        new RedisStreamNotificationSender(redisNotificationTemplate)
                });
            case REPLY:
                return new CompositeNotificationSender(new NotificationSender[]{
                        new FcmNotificationSender(firebaseMessaging),
                        new ReplyNotification(redisTemplate)
                });
            default:
                throw new IllegalArgumentException("Unsupported Notification Type: " + type);
        }
    }
}
