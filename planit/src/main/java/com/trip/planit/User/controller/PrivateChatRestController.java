package com.trip.planit.User.controller;

import com.trip.planit.User.apiPayload.code.status.ErrorStatus;
import com.trip.planit.User.apiPayload.exception.GeneralException;
import com.trip.planit.User.dto.ChatMessageDto;
import com.trip.planit.User.dto.ChatRequest;
import com.trip.planit.User.dto.ChatResponse;
import com.trip.planit.User.entity.ChatMessage;
import com.trip.planit.User.entity.ChatRoom;
import com.trip.planit.User.entity.User;
import com.trip.planit.User.repository.ChatMessageRepository;
import com.trip.planit.User.repository.UserRepository;
import com.trip.planit.User.service.PrivateChatService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/private/chatrooms")
public class PrivateChatRestController {

  private final PrivateChatService privateChatService;
  private final UserRepository userRepository;
  private final ChatMessageRepository chatMessageRepository;

  @Autowired
  public PrivateChatRestController(PrivateChatService privateChatService, UserRepository userRepository, ChatMessageRepository chatMessageRepository) {
    this.privateChatService = privateChatService;
    this.userRepository = userRepository;
    this.chatMessageRepository = chatMessageRepository;
  }

  @Operation(summary = "1:1 채팅방 생성", description = "두 사용자 간 1:1 채팅방을 생성합니다.")
  @PostMapping("/create")
  public ChatResponse create(@RequestBody ChatRequest req) {
    User a = userRepository.findByUserId(req.getRoomMakerId())
        .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
    User b = userRepository.findByUserId(req.getGuestId())
        .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

    ChatRoom room = privateChatService.createOrGetRoom(a, b);

    return ChatResponse.fromUsers(
        room.getId(),
        a,
        b,
        /* 마지막 메시지 */ "",
        /* 마지막 메시지 시간 */ ""
    );
  }

  @Operation(summary = "1:1 채팅방 목록 조회", description = "로그인한 사용자가 참여 중인 1:1 채팅방 목록 반환")
  @GetMapping("/user")
  public List<ChatResponse> list(Principal principal) {
    // 1) 현재 사용자 ID 조회
    Long me = userRepository.findByEmail(principal.getName())
        .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND))
        .getUserId();

    // 2) 활성화된 1:1 채팅방 조회
    List<ChatRoom> rooms = privateChatService.getActiveRooms(me);

    // 3) 채팅방별 ChatResponse 생성
    return rooms.stream().map(room -> {
      // 3-1) 상대방 정보 조회
      Long otherId = room.getUser1Id().equals(me) ? room.getUser2Id() : room.getUser1Id();
      User other  = userRepository.findByUserId(otherId)
          .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

      // 3-2) 마지막 메시지 한 건만 조회
      ChatMessage last = chatMessageRepository
          .findTop1ByChatRoom_IdOrderByCreatedAtDesc(room.getId())
          .orElse(null);

      String lastMsg  = last  != null ? last.getContent()        : "";
      String lastTime = last  != null ? last.getCreatedAt().toString() : "";

      // 3-3) DTO 빌드
      return ChatResponse.forList(
          room.getId(),
          room.getUser1Id(),
          room.getUser2Id(),
          other.getNickname(),
          other.getEmail(),
          lastMsg,
          lastTime
      );
    }).collect(Collectors.toList());
  }


  @Operation(summary = "1:1 채팅 기록 조회", description = "두 사용자 간 전체 메시지 내역 조회")
  @GetMapping("/history")
  public List<ChatMessageDto> history(
      @RequestParam String user1,
      @RequestParam String user2) {

    // 서비스 메소드 호출로 간결해짐
    return privateChatService.getUserChatHistory(user1, user2).stream()
        .map(m -> ChatMessageDto.builder()
            .id(m.getId())
            .senderNickname(m.getSender())
            .type(m.getType())
            .content(m.getContent())
            .timestamp(m.getCreatedAt())
            .build()
        )
        .collect(Collectors.toList());
  }
}
