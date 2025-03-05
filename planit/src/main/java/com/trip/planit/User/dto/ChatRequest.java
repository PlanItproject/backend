package com.trip.planit.User.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChatRequest {
    private String roomMakerId;
    private String guestId;
}
