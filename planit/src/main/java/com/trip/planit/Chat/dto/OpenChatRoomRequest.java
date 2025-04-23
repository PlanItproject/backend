package com.trip.planit.Chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class OpenChatRoomRequest extends ChatRoomRequest {
  /** 방 이름 */
  @NotBlank
  private String roomName;
}