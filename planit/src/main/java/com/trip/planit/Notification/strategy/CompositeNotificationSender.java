package com.trip.planit.Notification.strategy;

import com.trip.planit.Notification.model.Notification;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CompositeNotificationSender implements NotificationSender {
    private final NotificationSender[] senders;

    @Override
    public void send(Notification notification) {
        for (NotificationSender sender : senders) {
            sender.send(notification);
        }
    }
}
