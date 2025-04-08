package com.trip.planit.Notification.strategy.chat;

import com.trip.planit.Notification.constants.NotificationConstants;
import com.trip.planit.Notification.exception.NotificationException;
import com.trip.planit.Notification.model.Notification;
import com.trip.planit.Notification.strategy.NotificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
@RequiredArgsConstructor
public class ChatNotificationSender implements NotificationSender {

    private final KafkaTemplate<String, Notification> kafkaTemplate;

    @Override
    public void send(Notification notification) {
        try {
            kafkaTemplate.send(NotificationConstants.TOPIC_CHAT, notification);
            System.out.println("chat notification : " + notification.getMessage());
        } catch (Exception e) {
            throw new NotificationException("Failed to send chat notification", e);
        }
    }
}
