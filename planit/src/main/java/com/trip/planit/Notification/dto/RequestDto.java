package com.trip.planit.Notification.dto;

import lombok.Data;

@Data
public class RequestDto {
    private Long userId;
    private String fcmToken;
}