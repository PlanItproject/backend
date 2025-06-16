package com.trip.planit.Notification.strategy.fcm;

import com.google.firebase.messaging.AndroidConfig;
import com.trip.planit.Notification.exception.NotificationException;
import com.trip.planit.Notification.model.Notification;
import com.trip.planit.Notification.strategy.NotificationSender;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FcmNotificationSender implements NotificationSender {
    private final FirebaseMessaging firebaseMessaging;

    @Override
    public void send(Notification notification) {
        try {
            Message message = Message.builder()
                    .setToken(notification.getTargetToken())
                    .setNotification(
                            com.google.firebase.messaging.Notification.builder()
                                    .setTitle("🌍 Planit 알림")
                                    .setBody(notification.getMessage())
                                    .build()
                    )
                    .putData("title", "🌍 Planit 알림")
                    .putData("body", notification.getMessage())
                    .putData("notificationType", notification.getType().name())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build())
                    .build();

            String response = firebaseMessaging.send(message);
            System.out.println("✅ FCM notification sent successfully: " + response);

        } catch (Exception e) {
            e.printStackTrace(); // 무조건 추가!!
            throw new NotificationException("Failed to send FCM notification", e);
        }
    }
}
