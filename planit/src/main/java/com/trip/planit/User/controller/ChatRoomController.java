//package com.trip.planit.User.controller;
//
//import com.trip.planit.User.dto.ChatRequest;
//import com.trip.planit.User.entity.ChatMessage;
//import com.trip.planit.User.entity.ChatRoom;
//import com.trip.planit.User.entity.User;
//import com.trip.planit.User.repository.UserRepository;
//import com.trip.planit.User.service.ChatRoomService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//import java.util.List;
//
//@RestController
//@RequestMapping("/chatrooms")
//public class ChatRoomController {
//
//    private final ChatRoomService chatRoomService;
//    private final UserRepository userRepository;
//
//    @Autowired
//    public ChatRoomController(ChatRoomService chatRoomService, UserRepository userRepository) {
//        this.chatRoomService = chatRoomService;
//        this.userRepository = userRepository;
//    }
//
//    @PostMapping("/create")
//    public ChatRoom createChatRoom(@RequestBody ChatRequest chatRequest) {
//        User user1 = userRepository.findByUserId(chatRequest.getRoomMakerId())
//                .orElseThrow(() -> new RuntimeException("User not found: " + chatRequest.getRoomMakerId()));
//        User user2 = userRepository.findByUserId(chatRequest.getGuestId())
//                .orElseThrow(() -> new RuntimeException("User not found: " + chatRequest.getGuestId()));
//
//        return chatRoomService.createChatRoom(user1, user2);
//    }
//
//    // 사용자가 참여 중인 채팅방 목록 조회 (예: GET /chatrooms/user/{userId})
//    @GetMapping("/user/{userId}")
//    public List<ChatRoom> getChatRooms(@PathVariable Long userId) {
//        return chatRoomService.getChatRoomsForUser(userId);
//    }
//
//    // 특정 채팅방의 전체 메시지 내역 조회 (예: GET /chatrooms/messages/{chatRoomId})
//    @GetMapping("/messages/{chatRoomId}")
//    public List<ChatMessage> getChatMessages(@PathVariable String chatRoomId) {
//        return chatRoomService.getChatMessages(chatRoomId);
//    }
//}
