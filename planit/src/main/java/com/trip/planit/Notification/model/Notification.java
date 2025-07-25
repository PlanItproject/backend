package com.trip.planit.Notification.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    private NotificationSender sender;

    private String targetToken;

    public Notification(String message, NotificationType type, NotificationSender sender, String targetToken) {
        this.message = message;
        this.type = type;
        this.sender = sender;
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
        this.targetToken = targetToken;
    }

    public void send() {
        sender.send(this);
    }

    public void read() {
        this.isRead = true;
    }
}
