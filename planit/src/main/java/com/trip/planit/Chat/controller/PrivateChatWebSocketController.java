//package com.trip.planit.Chat.controller;
//
//import com.trip.planit.User.apiPayload.code.status.ErrorStatus;
//import com.trip.planit.User.apiPayload.exception.GeneralException;
//import com.trip.planit.User.entity.ChatMessage;
//import com.trip.planit.User.entity.ChatRoom;
//import com.trip.planit.Chat.entity.MessageType;
//import com.trip.planit.User.entity.User;
//import com.trip.planit.User.repository.UserRepository;
//import com.trip.planit.Chat.service.PrivateChatService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.handler.annotation.*;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Controller;
//
//import java.security.Principal;
//
//@Controller
//public class PrivateChatWebSocketController {
//
//  private final SimpMessagingTemplate simpMessagingTemplate;
//  private final PrivateChatService privateChatService;
//  private final UserRepository userRepo;
//
//  @Autowired
//  public PrivateChatWebSocketController(SimpMessagingTemplate simpMessagingTemplate, PrivateChatService privateChatService, UserRepository userRepository) {
//    this.simpMessagingTemplate = simpMessagingTemplate;
//    this.privateChatService = privateChatService;
//    this.userRepo = userRepository;
//  }
//
//  @MessageMapping("/chat.sendPrivateMessage")
//  public void sendPrivate(@Payload ChatMessage chatMessage, Principal principal) {
//    if (principal == null)
//      throw new GeneralException(ErrorStatus.NOT_AUTHENTICATED);
//
//    String me = principal.getName();
//    chatMessage.setSender(me);
//
//    User s = userRepo.findByEmail(me)
//        .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
//    User r = userRepo.findByEmail(chatMessage.getReceiver())
//        .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
//
//    ChatRoom room = privateChatService.createOrGetRoom(s, r);
//    chatMessage.setChatRoom(room);
//    chatMessage.setType(MessageType.PRIVATE);
//
//    ChatMessage saved = privateChatService.saveMessage(chatMessage);
//    simpMessagingTemplate.convertAndSendToUser(r.getEmail(), "/queue/private", saved);
//  }
//
//  @MessageMapping("/chat.markAsRead")
//  public void read(@Payload Long msgId) {
//    privateChatService.markAsRead(msgId);
//  }
//
//  @MessageMapping("/chat.leave")
//  public void leave(@Payload Long roomId, Principal principal) {
//    privateChatService.leaveRoom(
//        roomId,
//        userRepo.findByEmail(principal.getName())
//            .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND))
//            .getUserId());
//  }
//}
