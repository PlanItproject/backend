package com.trip.planit.Notification.model;

import com.trip.planit.Notification.strategy.NotificationSender;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Notification {
    private Long id;
    private String message;
    private LocalDateTime createdAt;
    private boolean isRead;
    private NotificationType type;

    private NotificationSender sender;

    public Notification(String message, NotificationType type, NotificationSender sender) {
        this.message = message;
        this.type = type;
        this.sender = sender;
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    public void send() {
        sender.send(this);
    }

    public void read() {
        this.isRead = true;
    }
}
