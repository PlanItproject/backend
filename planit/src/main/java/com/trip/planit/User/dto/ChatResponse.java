package com.trip.planit.User.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChatResponse {
    private Long roomMakerId;
    private Long guestId;
    private Long chatRoomId;
    private String roomMakerNickName;
    private String guestNickName;
    private String roomMakerEmail;
    private String guestEmail;
}