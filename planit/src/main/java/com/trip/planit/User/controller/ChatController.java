package com.trip.planit.User.controller;

import com.trip.planit.User.entity.ChatMessage;
import com.trip.planit.User.entity.ChatRoom;
import com.trip.planit.User.entity.User;
import com.trip.planit.User.repository.ChatMessageRepository;
import com.trip.planit.User.repository.UserRepository;
import com.trip.planit.User.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomService chatRoomService;
    private final UserRepository userRepository;

    @Autowired
    public ChatController(SimpMessagingTemplate messagingTemplate, ChatMessageRepository chatMessageRepository,
        ChatRoomService chatRoomService, UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomService = chatRoomService;
        this.userRepository = userRepository;
    }

    // 1:1 채팅 메시지 전송 처리 (현재 인증된 사용자의 정보를 사용)
    @MessageMapping("/chat.sendPrivateMessage")
    public void sendPrivateMessage(ChatMessage chatMessage, Principal principal) {
        if (principal == null) {
            System.err.println("전송 실패: 인증된 사용자 정보(Principal)가 없습니다.");
            return;
        }

        String senderEmail = principal.getName();
        chatMessage.setSender(senderEmail);
        System.out.println("메시지 전송 요청 - Sender: " + senderEmail + ", Receiver: " + chatMessage.getReceiver());

        // sender와 receiver 이메일을 기반으로 User 엔티티 조회
        User sender = userRepository.findByEmail(senderEmail)
            .orElseThrow(() -> new RuntimeException("Sender not found: " + senderEmail));
        User receiver = userRepository.findByEmail(chatMessage.getReceiver())
            .orElseThrow(() -> new RuntimeException("Receiver not found: " + chatMessage.getReceiver()));

        // 채팅방 생성 또는 기존 채팅방 조회
        ChatRoom chatRoom = chatRoomService.createChatRoom(sender, receiver);
        chatMessage.setChatRoom(chatRoom);

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        messagingTemplate.convertAndSendToUser(
                chatMessage.getReceiver(), "/queue/private", savedMessage);

        System.out.println("메시지 전송 완료: " + savedMessage.getId());
    }

    // 클라이언트가 메시지를 읽었을 때 호출되는 메서드
    @MessageMapping("/chat.markAsRead")
    public void markMessageAsRead(@Payload Long messageId) {
        Optional<ChatMessage> optionalMessage = chatMessageRepository.findById(messageId);
        if (optionalMessage.isPresent()) {
            ChatMessage message = optionalMessage.get();
            message.setRead(true);
            chatMessageRepository.save(message);

            // **고침**: 읽음 리시트를 보내기 위해, 메시지를 보낸 사용자의 식별자를 가져온 후 읽음 리시트를 전송합니다.
            String sender = message.getSender();
            // sender에게 읽음 리시트 메시지 전송 (예: 메시지 ID 전송)
            messagingTemplate.convertAndSendToUser(sender, "/queue/read-receipt", messageId);
            System.out.println("메시지 " + messageId + " 읽음 처리 완료 및 리시트 전송됨.");
        }
    }

    @GetMapping("/history")
    public List<ChatMessage> getChatHistory(@RequestParam String user1, @RequestParam String user2) {
        return chatMessageRepository.findChatHistory(user1, user2);
    }

}
