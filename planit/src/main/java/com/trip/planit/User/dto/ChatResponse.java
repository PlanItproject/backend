package com.trip.planit.User.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

/**
 * 채팅방 생성/목록 조회 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {
    private Long roomMakerId;
    private Long guestId;
    private Long chatRoomId;
    private String roomMakerNickname;
    private String guestNickname;
    private String roomMakerEmail;
    private String guestEmail;
    private String otherNickname;
    private String otherEmail;
    private String lastMessage;
    private String lastMessageTime;

    /** 채팅방 생성 응답 빌더 */
    public static ChatResponse fromUsers(Long chatRoomId,
        com.trip.planit.User.entity.User maker,
        com.trip.planit.User.entity.User guest,
        String lastMessage,
        String lastMessageTime) {
        return ChatResponse.builder()
            .roomMakerId(maker.getUserId())
            .guestId(guest.getUserId())
            .chatRoomId(chatRoomId)
            .roomMakerNickname(maker.getNickname())
            .guestNickname(guest.getNickname())
            .roomMakerEmail(maker.getEmail())
            .guestEmail(guest.getEmail())
            .otherNickname("")   // 생성 시에는 상대 정보 없음
            .otherEmail("")
            .lastMessage(lastMessage)
            .lastMessageTime(lastMessageTime)
            .build();
    }

    /** 채팅방 목록 조회 응답 빌더 */
    public static ChatResponse forList(Long chatRoomId,
        Long makerId,
        Long guestId,
        String otherNickname,
        String otherEmail,
        String lastMessage,
        String lastMessageTime) {
        return ChatResponse.builder()
            .roomMakerId(makerId)
            .guestId(guestId)
            .chatRoomId(chatRoomId)
            .roomMakerNickname("")
            .guestNickname("")
            .roomMakerEmail("")
            .guestEmail("")
            .otherNickname(otherNickname)
            .otherEmail(otherEmail)
            .lastMessage(lastMessage)
            .lastMessageTime(lastMessageTime)
            .build();
    }
}
