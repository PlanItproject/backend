package com.trip.planit.Notification.service;

import com.trip.planit.Notification.entity.NotificationEntity;
import com.trip.planit.Notification.exception.NotificationException;
import com.trip.planit.Notification.strategy.NotificationSenderFactory;
import com.trip.planit.Notification.model.Notification;
import com.trip.planit.Notification.model.NotificationType;
import com.trip.planit.Notification.repository.NotificationRepository;
import com.trip.planit.User.entity.User;
import com.trip.planit.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationSenderFactory notificationSenderFactory;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public void sendNotification(String message, Long userId, NotificationType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotificationException("User not found"));

        String userToken = user.getFcmToken();
        if (userToken == null || userToken.isEmpty()) {
            throw new NotificationException("User has no FCM token registered");
        }

        Notification notification = new Notification(
                message,
                type,
                notificationSenderFactory.createSender(type),
                userToken
        );

        notification.send();

        NotificationEntity notificationEntity = NotificationEntity.builder()
                .userId(userId)
                .message(message)
                .isRead(false)
                .type(type)
                .createdAt(LocalDateTime.now())
                .targetToken(userToken)
                .build();

        notificationRepository.save(notificationEntity);
    }

    @Transactional
    public void markNotificationAsRead(Long notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException("Notification not found"));
        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    public List<NotificationEntity> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }
}
