package com.trip.planit.Chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
// 메시지 전송 요청용 DTO (JPA 엔티티와 분리)
@Getter @Setter
public class ChatMessageRequest {
  private Long chatRoomId;
  private String content;
  private Long receiverId;
}