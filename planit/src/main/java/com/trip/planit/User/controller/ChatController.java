package com.trip.planit.User.controller;

import com.trip.planit.User.entity.ChatMessage;
import com.trip.planit.User.repository.ChatMessageRepository;
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

    @Autowired
    public ChatController(SimpMessagingTemplate messagingTemplate, ChatMessageRepository chatMessageRepository) {
        this.messagingTemplate = messagingTemplate;
        this.chatMessageRepository = chatMessageRepository;
    }

    // 1:1 채팅 메시지 전송 처리 (현재 인증된 사용자의 정보를 사용)
    @MessageMapping("/chat.sendPrivateMessage")
    public void sendPrivateMessage(ChatMessage chatMessage, Principal principal) {
        if (principal == null) {
            System.err.println("전송 실패: 인증된 사용자 정보(Principal)가 없습니다.");
            return;
        }

        String sender = principal.getName();
        chatMessage.setSender(sender);

        // **고침**: 추가 로그 - sender와 receiver 출력
        System.out.println("메시지 전송 요청 - Sender: " + sender + ", Receiver: " + chatMessage.getReceiver());

        // **고침**: 채팅 메시지를 DB에 저장 (채팅 내역 보관)
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        // **고침**: 저장된 메시지를 전송 (추가 정보 포함)
        messagingTemplate.convertAndSendToUser(
                chatMessage.getReceiver(), "/queue/private", savedMessage);

        // **고침**: 전송 후 로그 출력
        System.out.println("메시지 전송 완료: " + savedMessage.getId());
    }

    // 클라이언트가 메시지를 읽었을 때 호출되는 메서드
// 읽음 리시트 전송 추가 (**고침**)
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
