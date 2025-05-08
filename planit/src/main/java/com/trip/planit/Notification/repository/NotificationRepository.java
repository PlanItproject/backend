package com.trip.planit.Notification.repository;

import com.trip.planit.Notification.entity.NotificationEntity;
import com.trip.planit.Notification.model.NotificationType;
import com.trip.planit.User.entity.User;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByUserAndIsReadFalse(User user);

    List<NotificationEntity> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<NotificationEntity> findByType(NotificationType type);

}
