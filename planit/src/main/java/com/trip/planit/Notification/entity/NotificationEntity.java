package com.trip.planit.Notification.entity;

import com.trip.planit.Notification.model.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String message;

    private boolean isRead;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private LocalDateTime createdAt;

    private String targetToken; // FCM 발송용 토큰
}
