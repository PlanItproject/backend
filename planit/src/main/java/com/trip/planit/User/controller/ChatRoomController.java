package com.trip.planit.User.controller;

import com.trip.planit.User.dto.ChatRequest;
import com.trip.planit.User.entity.ChatMessage;
import com.trip.planit.User.entity.ChatRoom;
import com.trip.planit.User.entity.User;
import com.trip.planit.User.repository.ChatRoomRepository;
import com.trip.planit.User.repository.UserRepository;
import com.trip.planit.User.service.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chatrooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatroomRepository;

    @Autowired
    public ChatRoomController(ChatRoomService chatRoomService, UserRepository userRepository, ChatRoomRepository chatroomRepository) {
        this.chatRoomService = chatRoomService;
        this.userRepository = userRepository;
        this.chatroomRepository = chatroomRepository;
    }

    @Operation(summary = "채팅방 생성 API", description = "채팅방을 생성하는 API")
    @PostMapping("/create")
    public ChatRoom createChatRoom(@RequestBody ChatRequest chatRequest) {
        System.out.println("Received ChatRequest: " + chatRequest);

        User user1 = userRepository.findByUserId(chatRequest.getRoomMakerId())
                .orElseThrow(() -> new RuntimeException("User not found: " + chatRequest.getRoomMakerId()));
        User user2 = userRepository.findByUserId(chatRequest.getGuestId())
                .orElseThrow(() -> new RuntimeException("User not found: " + chatRequest.getGuestId()));

        return chatRoomService.createChatRoom(user1, user2);
    }

    @Operation(summary = "채팅방 참가 API", description = "기존 채팅방에 참가합니다.")
    @PostMapping("/join")
    public ChatRoom joinChatRoom(@RequestBody ChatRequest chatRequest) {
        // 1:1 채팅의 경우 이미 생성된 채팅방에 두 사용자 모두 참여되어 있으므로, 해당 채팅방 정보를 반환합니다.
        return getChatRoomById(chatRequest.getRoomMakerId());
    }

    // 사용자가 포함된 채팅방 전체 조회 (GET /chatrooms/user/{userId})
    @GetMapping("/user/{userId}")
    public List<ChatRoom> getChatRooms(@PathVariable Long userId) {
        return chatRoomService.getChatRoomsForUser(userId);
    }

    // 특정 채팅방의 전체 메시지 내역 조회 (GET /chatrooms/history/{chatRoomId})
    @GetMapping("/history/{chatRoomId}")
    public List<ChatMessage> getChatHistory(@PathVariable Long chatRoomId) {
        return chatRoomService.getChatHistory(chatRoomId);
    }

    // 채팅방 ID로 조회 (내부 사용)
    public ChatRoom getChatRoomById(Long chatRoomId) {
        return chatroomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found: " + chatRoomId));
    }
}
