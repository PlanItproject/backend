package com.trip.planit.Notification.controller;

import com.trip.planit.Notification.dto.NotificationDto;
import com.trip.planit.Notification.dto.RequestDto;
import com.trip.planit.Notification.model.NotificationType;
import com.trip.planit.Notification.service.NotificationService;
import com.trip.planit.User.apiPayload.exception.ApiResponse;
import com.trip.planit.User.entity.User;
import com.trip.planit.User.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    //    읽음처리
    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "읽음 처리", description = "안읽은 알림 읽음처리")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markNotificationAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    //    읽지 않은 알림 조회
    @GetMapping("/unread/{userId}")
    @Operation(summary = "읽지 않은 알림 조회", description = "읽지 않은 알림 조회")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        List<NotificationDto> unreadNotifications = notificationService.getUnreadNotifications(user);
        return ResponseEntity.ok(unreadNotifications);
    }


    //  테스트 알림(댓글)
    @PostMapping("/test-send/reply")
    @Operation(summary = "테스트 알림(댓글)", description = "테스트 알림 발송 (댓글)")
    public ResponseEntity<Void> sendReplyTestNotification(
            @RequestParam Long userId,
            @RequestParam String message
    ) {
        notificationService.sendNotification(message, userId, NotificationType.REPLY);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/test-send/chat")
    @Operation(summary = "테스트 알림(채팅)", description = "테스트 알림 발송 (채팅)")
    public ResponseEntity<Void> sendChatTestNotification(
            @RequestParam Long userId,
            @RequestParam String message
    ) {
        notificationService.sendNotification(message, userId, NotificationType.CHAT);
        return ResponseEntity.ok().build();
    }

    //    토큰 업데이트
    @PostMapping("/update-fcm-token")
    @Operation(summary = "fcm 토큰 업데이트", description = "fcm 토큰 db 저장")
    public ApiResponse<?> updateFcmToken(@RequestBody RequestDto request) {
        userService.updateFcmToken(request.getUserId(), request.getFcmToken());
        return ApiResponse.onSuccess("FCM 토큰 업데이트 성공");
    }

    @GetMapping
    @Operation(summary = "전체 알림 조회", description = "전체 알림 조회")
    public ResponseEntity<List<NotificationDto>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/date")
    @Operation(summary = "날짜 별 알림 조회", description = "날짜 별 알림 조회 ex) 20250430")
    public ResponseEntity<List<NotificationDto>> getByDate(@RequestParam("date") String date) {
        LocalDate parsedDate = LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd
        List<NotificationDto> result = notificationService.getNotificationsByDate(parsedDate);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/type")
    @Operation(summary = "알림/댓글 기준 별 알림 조회", description = "알림/댓글 기준 별 알림 조회")
    public ResponseEntity<List<NotificationDto>> getByType(@RequestParam NotificationType type) {
        return ResponseEntity.ok(notificationService.getNotificationsByType(type));
    }

    @GetMapping("/nickname")
    @Operation(summary = "닉네임 별 조회", description = "닉네임 별 알림 조회")
    public ResponseEntity<Long> getUserIdByNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(notificationService.getUserIdByNickname(nickname));
    }
}
