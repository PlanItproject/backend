package com.trip.planit.User.dto;

import com.trip.planit.User.entity.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * WebSocket을 통해 주고받는 메시지 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private Long id;
    private String senderNickname;
    private MessageType type;
    private String content;
    private LocalDateTime timestamp;
}
