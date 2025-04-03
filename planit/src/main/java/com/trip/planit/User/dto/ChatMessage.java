package com.trip.planit.User.dto;

import com.trip.planit.User.entity.MessageType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessage {
    private Long id;
    private String sender_nickname;
    private MessageType type;
    private String content;
    private LocalDateTime timestamp;
}



