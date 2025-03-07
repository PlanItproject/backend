package com.trip.planit.User.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChatResponse {
    private String roomMakerId;
    private String guestId;
    private String chatRoomId;
}