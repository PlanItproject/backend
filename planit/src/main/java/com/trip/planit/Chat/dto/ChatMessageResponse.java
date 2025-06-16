package com.trip.planit.Chat.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResponse {
  private Long messageId;
  private Long senderId;
  private String senderNickname;
  private Long receiverId;
  private String receiverNickname;
  private String content;
  private LocalDateTime sentAt;
}
