package com.trip.planit.Notification.strategy;

import com.trip.planit.Notification.model.Notification;

public interface NotificationSender {
    void send(Notification notification);
}
