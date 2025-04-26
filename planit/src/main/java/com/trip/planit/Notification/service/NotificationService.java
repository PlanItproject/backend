package com.trip.planit.Notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.trip.planit.Notification.exception.NotificationException;
import com.trip.planit.Notification.model.Notification;
import com.trip.planit.Notification.model.NotificationType;
import com.trip.planit.Notification.strategy.CompositeNotificationSender;
import com.trip.planit.Notification.strategy.NotificationSender;
import com.trip.planit.Notification.strategy.chat.ChatNotificationSender;
import com.trip.planit.Notification.strategy.fcm.FcmNotificationSender;
import com.trip.planit.Notification.strategy.reply.ReplyNotification;
import com.trip.planit.Notification.util.RedisDuplicateChecker;
import com.trip.planit.User.entity.User;
import com.trip.planit.User.repository.UserRepository;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final KafkaTemplate<String, Notification> kafkaTemplate;
    private final RedisTemplate<String, Notification> redisTemplate;
    private final FirebaseMessaging firebaseMessaging;
    private final RedisDuplicateChecker redisDuplicateChecker;
    private final UserRepository userRepository; // ✨ 추가

    public void sendChatNotification(String message, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotificationException("User not found"));

        String userToken = user.getFcmToken();
        if (userToken == null || userToken.isEmpty()) {
            throw new NotificationException("User has no FCM token registered");
        }

        String key = "notification:" + userId + ":chat";
        if (redisDuplicateChecker.isDuplicate(key)) return;

        NotificationSender sender = new CompositeNotificationSender(new NotificationSender[]{
                new FcmNotificationSender(firebaseMessaging),
                new ChatNotificationSender(kafkaTemplate)
        });

        Notification notification = new Notification(message, NotificationType.CHAT, sender, userToken);
        notification.send();
        redisDuplicateChecker.markAsSent(key, Duration.ofMinutes(5));
    }

    public void sendReplyNotification(String message, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotificationException("User not found"));

        String userToken = user.getFcmToken();
        if (userToken == null || userToken.isEmpty()) {
            throw new NotificationException("User has no FCM token registered");
        }

        String key = "notification:" + userId + ":reply";
        if (redisDuplicateChecker.isDuplicate(key)) return;

        NotificationSender sender = new CompositeNotificationSender(new NotificationSender[]{
                new FcmNotificationSender(firebaseMessaging),
                new ReplyNotification(redisTemplate)
        });

        Notification notification = new Notification(message, NotificationType.REPLY, sender, userToken);
        notification.send();
        redisDuplicateChecker.markAsSent(key, Duration.ofMinutes(5));
    }

    public void markNotificationAsRead(String notificationKey) {
        Notification notification = redisTemplate.opsForValue().get(notificationKey);
        if (notification == null) {
            throw new NotificationException("Notification not found for key: " + notificationKey);
        }

        notification.read(); // 읽음 처리
        redisTemplate.opsForValue().set(notificationKey, notification); // 다시 저장
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        String pattern = "notification:" + userId + ":*";

        Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
                .getConnection()
                .scan(ScanOptions.scanOptions().match(pattern).count(1000).build());

        List<Notification> unreadNotifications = new java.util.ArrayList<>();

        while (cursor.hasNext()) {
            String key = new String(cursor.next());
            Notification notification = redisTemplate.opsForValue().get(key);
            if (notification != null && !notification.isRead()) {
                unreadNotifications.add(notification);
            }
        }

        return unreadNotifications;
    }
}