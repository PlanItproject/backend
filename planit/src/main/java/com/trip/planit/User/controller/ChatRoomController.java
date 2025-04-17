package com.trip.planit.User.controller;

import com.trip.planit.User.apiPayload.code.status.ErrorStatus;
import com.trip.planit.User.apiPayload.exception.GeneralException;
import com.trip.planit.User.dto.ChatRequest;
import com.trip.planit.User.dto.ChatResponse;
import com.trip.planit.User.entity.ChatMessage;
import com.trip.planit.User.entity.ChatRoom;
import com.trip.planit.User.entity.User;
import com.trip.planit.User.repository.ChatMessageRepository;
import com.trip.planit.User.repository.ChatRoomRepository;
import com.trip.planit.User.repository.UserRepository;
import com.trip.planit.User.service.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import java.security.Principal;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chatrooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatroomRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Autowired
    public ChatRoomController(ChatRoomService chatRoomService, UserRepository userRepository, ChatRoomRepository chatroomRepository, ChatMessageRepository chatMessageRepository) {
        this.chatRoomService = chatRoomService;
        this.userRepository = userRepository;
        this.chatroomRepository = chatroomRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Operation(summary = "채팅방 생성", description = "채팅방을 생성")
    @PostMapping("/create")
    public ChatResponse createChatRoom(@RequestBody ChatRequest chatRequest) {
        System.out.println("Received ChatRequest: " + chatRequest);

        User roomMaker = userRepository.findByUserId(chatRequest.getRoomMakerId())
            .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        User guest = userRepository.findByUserId(chatRequest.getGuestId())
            .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        ChatRoom chatRoom = chatRoomService.createChatRoom(roomMaker, guest);

        String lastMessage = "";
        String lastMessageTime = "";

        return new ChatResponse(
            roomMaker.getUserId(),
            guest.getUserId(),
            chatRoom.getId(),
            roomMaker.getNickname(),
            guest.getNickname(),
            roomMaker.getEmail(),
            guest.getEmail(),
            "",
            "",
            lastMessage,
            lastMessageTime
        );
    }

    @Operation(summary = "채팅방 참가 API", description = "기존 채팅방에 참가합니다.")
    @PostMapping("/join")
    public ChatRoom joinChatRoom(@RequestBody ChatRequest chatRequest) {
        return getChatRoomById(chatRequest.getRoomMakerId());
    }

    @Operation(summary = "사용자가 포함된 채팅방 조회",
        description = "로그인한 사용자가 참여한 채팅방 목록을 반환하며, 각 채팅방에 대해 상대방의 이름과 마지막 대화 내역(내용, 시간)을 포함합니다.")
    @GetMapping("/user")
    public List<ChatResponse> getChatRooms(Principal principal) {

        User currentUser = userRepository.findByEmail(principal.getName())
            .orElseThrow(() -> new RuntimeException("로그인한 사용자를 찾을 수 없습니다."));
        Long userId = currentUser.getUserId();

        List<ChatRoom> rooms = chatRoomService.getChatRoomsForUser(userId);
        List<ChatResponse> responses = new ArrayList<>();
        for (ChatRoom room : rooms) {
            List<ChatMessage> messages = chatMessageRepository.findAllByChatRoom_IdOrderByCreatedAtAsc(room.getId());
            String lastMessage = "";
            String lastMessageTime = "";
            if (!messages.isEmpty()) {
                ChatMessage lastMsg = messages.get(messages.size() - 1);
                lastMessage = lastMsg.getContent();
                lastMessageTime = lastMsg.getCreatedAt().toString();
            }

            User user1 = userRepository.findByUserId(room.getUser1Id()).orElse(null);
            User user2 = userRepository.findByUserId(room.getUser2Id()).orElse(null);
            String otherNickName = "";
            String otherEmail = "";
            if (user1 != null && user2 != null) {
                if (user1.getUserId().equals(userId)) {
                    otherNickName = user2.getNickname();
                    otherEmail = user2.getEmail();
                } else if (user2.getUserId().equals(userId)) {
                    otherNickName = user1.getNickname();
                    otherEmail = user1.getEmail();
                }
            }

            ChatResponse response = new ChatResponse(
                user1 != null ? user1.getUserId() : null,
                user2 != null ? user2.getUserId() : null,
                room.getId(),
                user1 != null ? user1.getNickname() : "",
                user2 != null ? user2.getNickname() : "",
                user1 != null ? user1.getEmail() : "",
                user2 != null ? user2.getEmail() : "",
                otherNickName,
                otherEmail,
                lastMessage,
                lastMessageTime
            );
            responses.add(response);
        }
        return responses;
    }

    @Operation(summary = "채팅방 전체 메시지 조회", description = "특정 채팅방의 전체 메시지 내역을 조회")
    @GetMapping("/history/{chatRoomId}")
    public List<ChatMessage> getChatHistory(@PathVariable Long chatRoomId) {
        return chatRoomService.getChatHistory(chatRoomId);
    }

    // 채팅방 ID로 조회 (내부 사용)
    public ChatRoom getChatRoomById(Long chatRoomId) {
        return chatroomRepository.findById(chatRoomId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_CUSTOM));
    }
}
