package com.trip.planit.Chat.dto;

import java.util.List;
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
public class OpenChatRoomResponse extends ChatRoomResponse {
  private Long creatorId;
  private String roomName;

  /** 오픈 채팅에서는 참가자 리스트를 보내고 싶다면 아래처럼 */
  private List<ParticipantDto> participants;
}
