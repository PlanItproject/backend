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

    // 수정됨: 상대방의 닉네임과 이메일(로그인한 사용자가 아닌 사용자 정보)
    private String otherNickName;
    private String otherEmail;

    // 수정됨: 마지막 메시지 내용과 전송시간
    private String lastMessage;
    private String lastMessageTime;
}