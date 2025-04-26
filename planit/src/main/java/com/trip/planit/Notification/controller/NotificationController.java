package com.trip.planit.Notification.controller;

import com.trip.planit.Notification.model.Notification;
import com.trip.planit.Notification.service.NotificationService;
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
    @PutMapping("/read")
    public ResponseEntity<Void> markAsRead(@RequestParam String notificationKey) {
        notificationService.markNotificationAsRead(notificationKey);
        return ResponseEntity.ok().build();
    }

//    토큰 업데이트
    @PostMapping("/{userId}/fcm-token")
    public ResponseEntity<Void> updateFcmToken(@PathVariable Long userId, @RequestBody String fcmToken) {
        userService.updateFcmToken(userId, fcmToken);
        return ResponseEntity.ok().build();
    }

//    읽지 않은 알림 조회
    @GetMapping("/unread/{userId}")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@PathVariable Long userId) {
        List<Notification> unreadNotifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(unreadNotifications);
    }

//  테스트 알림
    @PostMapping("/test-send")
    public ResponseEntity<Void> sendTestNotification(
            @RequestParam Long userId,
            @RequestParam String message
    ) {
        notificationService.sendChatNotification(message, userId);
        return ResponseEntity.ok().build();
    }
}
