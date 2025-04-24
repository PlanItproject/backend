package com.trip.planit.Notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.trip.planit.Notification.model.Notification;
import com.trip.planit.Notification.model.NotificationType;
import com.trip.planit.Notification.strategy.CompositeNotificationSender;
import com.trip.planit.Notification.strategy.NotificationSender;
import com.trip.planit.Notification.strategy.chat.ChatNotificationSender;
import com.trip.planit.Notification.strategy.fcm.FcmNotificationSender;
import com.trip.planit.Notification.strategy.reply.ReplyNotification;
import com.trip.planit.Notification.util.RedisDuplicateChecker;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final KafkaTemplate<String, Notification> kafkaTemplate;
    private final RedisTemplate<String, Notification> redisTemplate;
    private final FirebaseMessaging firebaseMessaging;
    private final RedisDuplicateChecker redisDuplicateChecker;

    public void sendChatNotification(String message, String userId) {
        String key = "notification:" + userId + ":chat";
        if (redisDuplicateChecker.isDuplicate(key)) return;

        NotificationSender sender = new CompositeNotificationSender(new NotificationSender[]{
                new FcmNotificationSender(firebaseMessaging),
                new ChatNotificationSender(kafkaTemplate)
        });

        Notification notification = new Notification(message, NotificationType.CHAT, sender);
        notification.send();
        redisDuplicateChecker.markAsSent(key, Duration.ofMinutes(5));
    }

    public void sendReplyNotification(String message, String userId) {
        String key = "notification:" + userId + ":reply";
        if (redisDuplicateChecker.isDuplicate(key)) return;

        NotificationSender sender = new CompositeNotificationSender(new NotificationSender[]{
                new FcmNotificationSender(firebaseMessaging),
                new ReplyNotification(redisTemplate)
        });

        Notification notification = new Notification(message, NotificationType.REPLY, sender);
        notification.send();
        redisDuplicateChecker.markAsSent(key, Duration.ofMinutes(5));
    }
}