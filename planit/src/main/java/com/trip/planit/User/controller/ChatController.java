//package com.trip.planit.User.controller;
//
//import com.trip.planit.User.entity.ChatMessage;
//import com.trip.planit.User.repository.ChatMessageRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Controller;
//
//import java.util.Optional;
//
//@Controller
//public class ChatController {
//
//    private final SimpMessagingTemplate messagingTemplate;
//    private final ChatMessageRepository chatMessageRepository;
//
//    @Autowired
//    public ChatController(SimpMessagingTemplate messagingTemplate, ChatMessageRepository chatMessageRepository) {
//        this.messagingTemplate = messagingTemplate;
//        this.chatMessageRepository = chatMessageRepository;
//    }
//
//    // 1:1 채팅 메시지 전송 처리
//    @MessageMapping("/chat.sendPrivateMessage")
//    public void sendPrivateMessage(@Payload ChatMessage chatMessage) {
//        // 메시지를 DB에 저장
//        chatMessageRepository.save(chatMessage);
//        // 수신자에게 메시지 전송 (수신자는 개별 큐를 구독)
//        messagingTemplate.convertAndSendToUser(
//                chatMessage.getReceiver(),
//                "/queue/private",
//                chatMessage
//        );
//    }
//
//    // 클라이언트가 메시지를 읽었을 때 호출되는 메서드
//    @MessageMapping("/chat.markAsRead")
//    public void markMessageAsRead(@Payload Long messageId) {
//        Optional<ChatMessage> optionalMessage = chatMessageRepository.findById(messageId);
//        if (optionalMessage.isPresent()) {
//            ChatMessage message = optionalMessage.get();
//            message.setRead(true);
//            chatMessageRepository.save(message);
//        }
//    }
//
//}
