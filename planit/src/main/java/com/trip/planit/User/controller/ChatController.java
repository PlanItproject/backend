package com.trip.planit.User.controller;

import com.trip.planit.User.apiPayload.code.status.ErrorStatus;
import com.trip.planit.User.apiPayload.exception.GeneralException;
import com.trip.planit.User.entity.ChatMessage;
import com.trip.planit.User.entity.ChatRoom;
import com.trip.planit.User.entity.MessageType;
import com.trip.planit.User.entity.User;
import com.trip.planit.User.repository.ChatMessageRepository;
import com.trip.planit.User.repository.UserRepository;
import com.trip.planit.User.service.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import java.time.LocalDateTime;
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

    @Operation(summary = "1:1 채팅 메시지", description = "1:1 채팅 메시지 전송 (쿠키 사용해서)")
    @MessageMapping("/chat.sendPrivateMessage")
    public void sendPrivateMessage(ChatMessage chatMessage, Principal principal) {
        if (principal == null) {
            throw new GeneralException(ErrorStatus.NOT_AUTHENTICATED);
        }

        String senderEmail = principal.getName();
        chatMessage.setSender(senderEmail);

        User sender = userRepository.findByEmail(senderEmail)
            .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        User receiver = userRepository.findByEmail(chatMessage.getReceiver())
            .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        ChatRoom chatRoom = chatRoomService.createChatRoom(sender, receiver);
        chatMessage.setChatRoom(chatRoom);
        chatMessage.setType(MessageType.PRIVATE);

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        messagingTemplate.convertAndSendToUser(
                chatMessage.getReceiver(), "/queue/private", savedMessage);

        System.out.println("메시지 전송 완료: " + savedMessage.getId());
    }

    @Operation(summary = "시스템 메시지 - 채팅 시작", description = "채팅 시작: [현재 날짜/시간]")
    @MessageMapping("/chat.start")
    public void sendChatStartNotification(@Payload Long chatRoomId, Principal principal) {
        if (principal == null) {
            throw new GeneralException(ErrorStatus.NOT_AUTHENTICATED);
        }
        ChatRoom chatRoom = chatRoomService.getChatRoomById(chatRoomId);

        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setChatRoom(chatRoom);
        systemMessage.setType(MessageType.SYSTEM);
        systemMessage.setSender("SYSTEM");
        systemMessage.setContent("채팅 시작: " + LocalDateTime.now());

        ChatMessage savedMessage = chatMessageRepository.save(systemMessage);
        messagingTemplate.convertAndSend("/topic/chatrooms/" + chatRoomId, savedMessage);

        System.out.println("채팅 시작 시스템 메시지 전송 완료: " + savedMessage.getContent());
    }

    @Operation(summary = "메시지 읽음 처리", description = "채팅 시작: [현재 날짜/시간]")
    @MessageMapping("/chat.markAsRead")
    public void markMessageAsRead(@Payload Long messageId) {
        Optional<ChatMessage> optionalMessage = chatMessageRepository.findById(messageId);
        if (optionalMessage.isPresent()) {
            ChatMessage message = optionalMessage.get();
            message.setRead(true);
            chatMessageRepository.save(message);

            String sender = message.getSender();
            messagingTemplate.convertAndSendToUser(sender, "/queue/read-receipt", messageId);
            System.out.println("메시지 " + messageId + " 읽음 처리 완료 및 리시트 전송됨.");
        }
    }

    @Operation(summary = "채팅방 나가기", description = "1:1")
    @MessageMapping("/chat.leave")
    public void sendChatLeaveNotification(@Payload Long chatRoomId, Principal principal) {
        if (principal == null) {
            throw new GeneralException(ErrorStatus.NOT_AUTHENTICATED);
        }
        String userEmail = principal.getName();
        User currentUser = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        Long userId = currentUser.getUserId();

        // leaveChatRoom 메서드를 호출하여 해당 사용자의 leave 상태만 업데이트합니다.
        chatRoomService.leaveChatRoom(chatRoomId, userId);

        System.out.println("사용자 " + userEmail + "가 채팅방 " + chatRoomId + "에서 나갔습니다.");
    }

    @GetMapping("/history")
    public List<ChatMessage> getChatHistory(@RequestParam String user1, @RequestParam String user2) {
        return chatMessageRepository.findChatHistory(user1, user2);
    }

}
