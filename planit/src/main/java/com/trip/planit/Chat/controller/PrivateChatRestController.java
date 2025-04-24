//package com.trip.planit.Chat.controller;
//
//import com.trip.planit.Chat.dto.ChatMessageDTO;
//import com.trip.planit.Chat.dto.PrivateChatRoomRequest;
//import com.trip.planit.Chat.dto.PrivateChatRoomResponse;
//import com.trip.planit.User.apiPayload.code.status.ErrorStatus;
//import com.trip.planit.User.apiPayload.exception.GeneralException;
//import com.trip.planit.User.entity.User;
//import com.trip.planit.Chat.repository.ChatMessageRepository;
//import com.trip.planit.User.repository.UserRepository;
//import com.trip.planit.Chat.service.PrivateChatService;
//import io.swagger.v3.oas.annotations.Operation;
//import jakarta.validation.Valid;
//import java.util.stream.Collectors;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.security.Principal;
//import java.util.List;
//
//@RestController
//@RequestMapping("/private")
//public class PrivateChatRestController {
//
//  private final PrivateChatService privateChatService;
//  private final UserRepository userRepository;
//  private final ChatMessageRepository chatMessageRepository;
//
//  @Autowired
//  public PrivateChatRestController(PrivateChatService privateChatService, UserRepository userRepository, ChatMessageRepository chatMessageRepository) {
//    this.privateChatService = privateChatService;
//    this.userRepository = userRepository;
//    this.chatMessageRepository = chatMessageRepository;
//  }
//
//  @Operation(
//      summary = "1:1 채팅방 생성",
//      description = "인증된 사용자와 요청된 상대방 간 1:1 채팅방을 생성하거나 기존 방을 반환합니다."
//  )
//  @PostMapping("/create")
//  public PrivateChatRoomResponse create(
//      @Valid @RequestBody PrivateChatRoomRequest request,
//      Principal principal
//  ) {
//    // principal.getName()에 담긴 이메일과 request(guestId)를 한 번에 서비스로 넘깁니다.
//    return privateChatService.createChatRoom(principal.getName(), request);
//  }
//
//  @GetMapping("/history/{guestId}")
//  public List<ChatMessageDTO> historyPath(
//      Principal principal,
//      @PathVariable Long guestId
//  ) {
//    String user1 = principal.getName();
//    User guest = userRepository.findById(guestId)
//        .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
//    String user2 = guest.getEmail();
//    return privateChatService.getUserChatHistory(user1, user2).stream()
//        .map(m -> new ChatMessageDTO(
//            m.getSender(), m.getContent(), m.getType(), m.getCreatedAt()
//        ))
//        .collect(Collectors.toList());
//  }
//
//}
