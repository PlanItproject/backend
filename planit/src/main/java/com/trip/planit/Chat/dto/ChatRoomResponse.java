package com.trip.planit.Chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class ChatRoomResponse {
  private Long chatRoomId;
  private String lastMessage;
  private String lastMessageTime;
}
