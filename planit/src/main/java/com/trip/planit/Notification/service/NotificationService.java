package com.trip.planit.Notification.service;

import com.trip.planit.Notification.dto.NotificationDto;
import com.trip.planit.Notification.entity.NotificationEntity;
import com.trip.planit.Notification.exception.NotificationException;
import com.trip.planit.Notification.strategy.NotificationSenderFactory;
import com.trip.planit.Notification.model.Notification;
import com.trip.planit.Notification.model.NotificationType;
import com.trip.planit.Notification.repository.NotificationRepository;
import com.trip.planit.User.entity.User;
import com.trip.planit.User.repository.UserRepository;
import java.time.LocalDate;
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
                .user(user)
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

    public List<NotificationDto> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalse(user).stream()
                .map(notification -> NotificationDto.builder()
                        .id(notification.getId())
                        .message(notification.getMessage())
                        .type(notification.getType())
                        .createdAt(notification.getCreatedAt())
                        .isRead(notification.isRead())
                        .build())
                .toList();
    }

    public List<NotificationDto> getAllNotifications() {
        return notificationRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    public List<NotificationDto> getNotificationsByType(NotificationType type) {
        return notificationRepository.findByType(type).stream()
                .map(this::convertToDto)
                .toList();
    }

    public Long getUserIdByNickname(String nickname) {
        return userRepository.findByNickname(nickname)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getUserId();
    }

    private NotificationDto convertToDto(NotificationEntity entity) {
        return NotificationDto.builder()
                .id(entity.getId())
                .message(entity.getMessage())
                .type(entity.getType())
                .createdAt(entity.getCreatedAt())
                .isRead(entity.isRead())
                .nickname(entity.getUser().getNickname()) // 추가
                .build();
    }

    public List<NotificationDto> getNotificationsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        return notificationRepository.findByCreatedAtBetween(startOfDay, endOfDay)
                .stream()
                .map(this::convertToDto)
                .toList();
    }
}
