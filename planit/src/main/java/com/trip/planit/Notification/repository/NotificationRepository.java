package com.trip.planit.Notification.repository;

import com.trip.planit.Notification.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByUserIdAndIsReadFalse(Long userId);
}
