package com.trip.planit.Chat.controller;

import com.trip.planit.Chat.dto.ChatMessageDTO;
import com.trip.planit.Chat.dto.ChatMessageResponse;
import com.trip.planit.Chat.dto.OpenChatRoomResponse;
import com.trip.planit.Chat.dto.ParticipantDto;
import com.trip.planit.Chat.entity.ChatMessage;
import com.trip.planit.Chat.entity.ChatParticipant;
import com.trip.planit.Chat.entity.OpenChatRoom;
import com.trip.planit.User.security.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import java.util.stream.Collectors;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import com.trip.planit.Chat.entity.ChatRoom;
import com.trip.planit.User.entity.User;
import com.trip.planit.Chat.service.OpenChatService;
import com.trip.planit.User.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/openchat")
public class OpenChatRestController {

  private final OpenChatService openChatService;
  private final UserService userService;
  SimpMessagingTemplate simpMessagingTemplate;

  @Autowired
  public OpenChatRestController(OpenChatService openChatService, UserService userService, SimpMessagingTemplate simpMessagingTemplate) {
    this.openChatService = openChatService;
    this.userService = userService;
    this.simpMessagingTemplate = simpMessagingTemplate;
  }


  /**
   * 1) 새 오픈채팅방 생성
   */
  @PostMapping("/create")
  public ResponseEntity<Map<String, Object>> create(
      @RequestParam String roomName,
      Principal principal
  ) {
    String email = principal.getName();
    User creator = userService.getUserByEmail(email);
    if (creator == null) {
      throw new EntityNotFoundException("User not found: " + email);
    }

    OpenChatRoom room = openChatService.createOpenRoom(
        creator.getUserId(), roomName
    );

    return ResponseEntity.ok(Map.of(
        "chatRoomId", room.getId(),
        "roomName", room.getRoomName()
    ));
  }

  /**
   * 2) 기존 방에 참가
   */
  @PostMapping("/join")
  public ResponseEntity<?> join(
      @RequestParam Long roomId,
      @RequestParam String nickname,
      Authentication auth
  ) {
    Long userId = ((CustomUserDetails) auth.getPrincipal()).getUserId();
    ChatParticipant p = openChatService.joinRoom(roomId, userId, nickname);

    // 시스템 메시지 생성·저장
    ChatMessage sys = openChatService.buildSystemMessage(
        roomId,
        userId,                        // senderId
        nickname,                      // sender (닉네임)
        nickname + "님이 입장했습니다."  // content
    );
    openChatService.saveMessage(sys);

    // WebSocket 브로드캐스트
    simpMessagingTemplate.convertAndSend(
        "/topic/chatrooms/" + roomId,
        new ChatMessageDTO(sys.getSender(), sys.getContent(), sys.getType(), sys.getCreatedAt())
    );
    simpMessagingTemplate.convertAndSend(
        "/topic/chatrooms/" + roomId + "/participants",
        openChatService.getNicknamesInRoom(roomId)
    );

    return ResponseEntity.ok(Map.of(
        "chatRoomId", roomId,
        "participantId", p.getId(),
        "nickname", p.getNickname()
    ));
  }

  @PostMapping("/kick")
  public ResponseEntity<Void> kick(
      @RequestParam Long roomId,
      @RequestParam String targetNickname,
      Authentication auth
  ) {
    // 1) JWT 에서 userId 꺼내오기
    Long userId = ((CustomUserDetails) auth.getPrincipal()).getUserId();

    // 2) 이제 서비스 메서드 시그니처도 (roomId, targetNickname, userId) 로 바꿔주세요
    openChatService.kick(roomId, targetNickname, userId);
    return ResponseEntity.ok().build();
  }

  /**
   * 전체 방 목록은 user 정보 불필요
   */
  @GetMapping("/rooms/all")
  public ResponseEntity<List<ChatRoom>> rooms() {
    return ResponseEntity.ok(openChatService.listRooms());
  }

  /**
   * 내가 만든/참여 중인 오픈채팅방만 조회
   */
  @GetMapping("/rooms/my")
  public ResponseEntity<List<OpenChatRoomResponse>> myRooms(Authentication auth) {
    Long userId = ((CustomUserDetails) auth.getPrincipal()).getUserId();

    List<OpenChatRoom> rooms = openChatService.getMyOpenRooms(userId);

    // Collectors.toList() 로 제네릭을 확실히 잡아줍니다.
    List<OpenChatRoomResponse> result = rooms.stream()
        .map(openRoom -> OpenChatRoomResponse.builder()
            .chatRoomId(openRoom.getId())
            .creatorId(openRoom.getCreatorId())
            .roomName(openRoom.getRoomName())
            .participants(
                openRoom.getParticipants().stream()
                    .filter(p -> p.getLeftAt() == null)
                    .map(p -> ParticipantDto.builder()
                        .userId(p.getUser().getUserId())
                        .nickname(p.getNickname())
                        .email(p.getUser().getEmail())
                        .joinedAt(p.getJoinedAt())
                        .build())
                    .collect(Collectors.toList())   // ← 여기
            )
            .build()
        )
        .collect(Collectors.toList());         // ← 그리고 여기

    return ResponseEntity.ok(result);
  }

  /** 특정 방의 (퇴장하지 않은) 참가자 닉네임 목록 조회 */
  @GetMapping("/rooms/{roomId}/participants")
  public ResponseEntity<List<String>> getParticipants(@PathVariable Long roomId) {
    List<String> nicknames = openChatService.getNicknamesInRoom(roomId);
    return ResponseEntity.ok(nicknames);
  }

  @GetMapping("/rooms/{roomId}/messages")
  public ResponseEntity<List<ChatMessageResponse>> getMessages(@PathVariable Long roomId) {
    List<ChatMessage> messages = openChatService.getMessages(roomId);

    List<ChatMessageResponse> dtoList = messages.stream()
        .map(m -> ChatMessageResponse.builder()
            .messageId(m.getId())
            .senderId(m.getSenderId())
            .senderNickname(m.getSender())
            .receiverId(m.getReceiverId())             // 매핑 추가
            .receiverNickname(m.getReceiver()) // 매핑 추가
            .content(m.getContent())
            .sentAt(m.getCreatedAt())
            .build())
        .collect(Collectors.toList());

    return ResponseEntity.ok(dtoList);
  }


}