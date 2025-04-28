package com.trip.planit.Notification.controller;

import com.trip.planit.Notification.dto.RequestDto;
import com.trip.planit.Notification.entity.NotificationEntity;
import com.trip.planit.Notification.model.NotificationType;
import com.trip.planit.Notification.service.NotificationService;
import com.trip.planit.User.apiPayload.exception.ApiResponse;
import com.trip.planit.User.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

//    읽음처리
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@RequestParam Long notificationId) {
        notificationService.markNotificationAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

//    읽지 않은 알림 조회
    @GetMapping("/unread/{userId}")
    public ResponseEntity<List<NotificationEntity>> getUnreadNotifications(@PathVariable Long userId) {
        List<NotificationEntity> unreadNotifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(unreadNotifications);
    }

//  테스트 알림
    @PostMapping("/test-send")
    public ResponseEntity<Void> sendTestNotification(
            @RequestParam Long userId,
            @RequestParam String message
    ) {
        notificationService.sendNotification(message, userId, NotificationType.CHAT);
        return ResponseEntity.ok().build();
    }


    //    토큰 업데이트
    @PostMapping("/update-fcm-token")
    public ApiResponse<?> updateFcmToken(@RequestBody RequestDto request) {
        userService.updateFcmToken(request.getUserId(), request.getFcmToken());
        return ApiResponse.onSuccess("FCM 토큰 업데이트 성공");
    }
}
