package com.trip.planit.Chat.controller;

import com.trip.planit.Chat.dto.ChatMessageRequest;
import com.trip.planit.Chat.dto.ChatMessageResponse;
import com.trip.planit.Chat.entity.ChatMessage;
import com.trip.planit.Chat.entity.ChatRoom;
import com.trip.planit.Chat.entity.PrivateChatRoom;
import com.trip.planit.User.apiPayload.code.status.ErrorStatus;
import com.trip.planit.User.apiPayload.exception.GeneralException;
import com.trip.planit.Chat.entity.MessageType;
import com.trip.planit.User.entity.User;
import com.trip.planit.User.repository.UserRepository;
import com.trip.planit.Chat.service.PrivateChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class PrivateChatWebSocketController {

  private final SimpMessagingTemplate simpMessagingTemplate;
  private final PrivateChatService privateChatService;
  private final UserRepository userRepo;

  @Autowired
  public PrivateChatWebSocketController(SimpMessagingTemplate simpMessagingTemplate, PrivateChatService privateChatService, UserRepository userRepository) {
    this.simpMessagingTemplate = simpMessagingTemplate;
    this.privateChatService = privateChatService;
    this.userRepo = userRepository;
  }

  @MessageMapping("/chat.sendPrivateMessage")
  public void sendPrivate(
      @Payload ChatMessageRequest req,
      Principal principal
  ) {

    if (principal == null) {
      throw new GeneralException(ErrorStatus.NOT_AUTHENTICATED);
    }

    // 1) sender/receiver 조회
    User sender = userRepo.findByEmail(principal.getName())
        .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
    User receiver = userRepo.findById(req.getReceiverId())
        .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

    // 2) 방 생성·조회
    PrivateChatRoom room = privateChatService
        .createOrGetRoom(sender.getUserId(), receiver.getUserId());

    // 3) 메시지 엔티티 생성 및 저장
    ChatMessage message = ChatMessage.builder()
        .chatRoom(room)
        .sender(sender.getEmail())
         .receiverId(receiver.getUserId()) // ← ID 기반 필드가 있다면 이걸로
        .type(MessageType.PRIVATE)
        .content(req.getContent())
        .build();

    ChatMessage saved = privateChatService.saveMessage(message);

    // 4) 응답용 DTO 생성
    ChatMessageResponse resp = ChatMessageResponse.builder()
        .messageId(saved.getId())
        .senderId(sender.getUserId())
        .senderNickname(sender.getNickname())
        .receiverId(receiver.getUserId())
        .receiverNickname(receiver.getNickname())
        .content(saved.getContent())
        .sentAt(saved.getCreatedAt())
        .build();

    // 5) 수신자 ID 채널로 전송
    simpMessagingTemplate.convertAndSendToUser(
        String.valueOf(receiver.getUserId()),
        "/queue/private",
        resp
    );
  }

  @MessageMapping("/chat.markAsRead")
  public void read(@Payload Long msgId) {
    privateChatService.markAsRead(msgId);
  }

  @MessageMapping("/chat.leave")
  public void leave(@Payload Long roomId, Principal principal) {
    privateChatService.leaveRoom(
        roomId,
        userRepo.findByEmail(principal.getName())
            .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND))
            .getUserId());
  }
}
