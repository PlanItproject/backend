package com.trip.planit.Chat.controller;

import com.trip.planit.Chat.dto.ChatMessageDTO;
import com.trip.planit.Chat.dto.ChatMessageRequest;
import com.trip.planit.User.apiPayload.code.status.ErrorStatus;
import com.trip.planit.User.apiPayload.exception.GeneralException;
import com.trip.planit.Chat.entity.ChatMessage;
import com.trip.planit.Chat.entity.ChatParticipant;
import com.trip.planit.Chat.entity.MessageType;
import com.trip.planit.Chat.service.OpenChatService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class OpenChatWebSocketController {

  private final SimpMessagingTemplate simpMessagingTemplate;
  private final OpenChatService openChatService;

  @Autowired
  public OpenChatWebSocketController(SimpMessagingTemplate messagingTemplate, OpenChatService openChatService) {
    this.simpMessagingTemplate = messagingTemplate;
    this.openChatService = openChatService;
  }

  @MessageMapping("/chat.sendMessage")
  public void send(
      @Payload ChatMessageRequest req,
      Principal principal
  ) {
    System.out.println("▶▶▶ chat.sendMessage called! payload=" + req);

    if (principal == null) {
      throw new GeneralException(ErrorStatus.NOT_AUTHENTICATED);
    }

    // 1) 보낸 사람 참가자 정보
    ChatParticipant p = openChatService.getParticipant(
        req.getChatRoomId(), principal.getName());

    // 2) JPA 엔티티로 변환
    ChatMessage msg = ChatMessage.builder()
        .chatRoom(p.getChatRoom())
        .content(req.getContent())
        .senderId(p.getUser().getUserId())
        .sender(p.getNickname())
        .type(MessageType.OPEN)
        .receiverId(req.getReceiverId())
        .receiver(p.getNickname())
        .build();

    // 3) DB에 저장
    ChatMessage saved = openChatService.saveMessage(msg);

    // 4) 클라이언트로 보낼 DTO
    ChatMessageDTO dto = new ChatMessageDTO(
        saved.getSender(),
        saved.getContent(),
        saved.getType(),
        saved.getCreatedAt()
    );

    // 5) WebSocket으로 브로드캐스트
    simpMessagingTemplate.convertAndSend(
        "/topic/chatrooms/" + req.getChatRoomId(),
        dto
    );
  }

  public record EnterPayload(Long roomId, String nickname) {}

  @MessageMapping("/chat.enter")
  public void enter(@Payload EnterPayload payload, Principal principal) {
    simpMessagingTemplate.convertAndSend(
        "/topic/chatrooms/" + payload.roomId() + "/participants",
        openChatService.getNicknamesInRoom(payload.roomId())
    );
  }


  public record LeavePayload(Long roomId) {}

  @MessageMapping("/chat.leaveRoom")
  public void leave(@Payload LeavePayload payload, Principal principal) {
    // 1) 퇴장 처리
    ChatParticipant p = openChatService.leaveRoom(
        payload.roomId(),
        principal.getName()
    );

    // 2) “퇴장” 시스템 메시지 브로드캐스트
    ChatMessage sys = openChatService.buildSystemMessage(
        payload.roomId(),
        p.getUser().getUserId(),                         // senderId
        p.getNickname(),                                 // sender
        "%s님이 퇴장했습니다.".formatted(p.getNickname())
    );
    ChatMessage saved = openChatService.saveMessage(sys);
    simpMessagingTemplate.convertAndSend(
        "/topic/chatrooms/" + payload.roomId(),
        saved
    );

    // 3) 업데이트된 참가자 목록 브로드캐스트
    List<String> nicks = openChatService.getNicknamesInRoom(payload.roomId());
    simpMessagingTemplate.convertAndSend(
        "/topic/chatrooms/" + payload.roomId() + "/participants",
        nicks
    );
  }

  /** 방장 강퇴 기능 */
  @MessageMapping("/chat.kick")
  public void kick(@Payload KickPayload payload, Principal principal) {
    if (principal == null) {
      throw new GeneralException(ErrorStatus.NOT_AUTHENTICATED);
    }

    // 1) 요청자(방장) 조회
    ChatParticipant requester = openChatService.getParticipant(
        payload.roomId(), principal.getName());

    // 2) 강퇴 처리
    openChatService.kick(
        payload.roomId(),
        payload.targetNickname(),
        requester.getUser().getUserId()
    );

    // 3) 시스템 메시지 생성 (senderId 추가) + 저장
    ChatMessage sys = openChatService.buildSystemMessage(
        payload.roomId(),
        requester.getUser().getUserId(),                  // senderId
        requester.getNickname(),                         // senderNickname
        payload.targetNickname() + "님이 강퇴되었습니다."
    );
    ChatMessage savedSys = openChatService.saveMessage(sys);

    // 4) 강퇴 메시지 브로드캐스트
    simpMessagingTemplate.convertAndSend(
        "/topic/chatrooms/" + payload.roomId(),
        new ChatMessageDTO(
            savedSys.getSender(),
            savedSys.getContent(),
            savedSys.getType(),
            savedSys.getCreatedAt()
        )
    );

    // 5) 변경된 참가자 목록 브로드캐스트
    simpMessagingTemplate.convertAndSend(
        "/topic/chatrooms/" + payload.roomId() + "/participants",
        openChatService.getNicknamesInRoom(payload.roomId())
    );
  }

  public record KickPayload(Long roomId, String targetNickname) {}
}