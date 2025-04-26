package com.trip.planit.Notification.strategy.fcm;

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
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle("Notification")
                            .setBody(notification.getMessage())
                            .build())
                    .putData("notificationType", notification.getType().name())
                    .build();

            firebaseMessaging.send(message);
            System.out.println("FCM notification sent: " + notification.getMessage());
        } catch (Exception e) {
            throw new NotificationException("Failed to send FCM notification", e);
        }
    }
}
