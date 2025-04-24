//package com.trip.planit.Chat.service;
//
//import com.trip.planit.Chat.entity.ChatRoom;
//import com.trip.planit.Chat.entity.ChatMessage;
//import com.trip.planit.Chat.repository.ChatMessageRepository;
//import com.trip.planit.Chat.repository.ChatRoomRepository;
//import com.trip.planit.User.entity.User;
//import jakarta.persistence.EntityNotFoundException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class ChatRoomService {
//
//    private final ChatRoomRepository chatRoomRepository;
//    private final ChatMessageRepository chatMessageRepository;
//
//    @Autowired
//    public ChatRoomService(ChatRoomRepository chatRoomRepository, ChatMessageRepository chatMessageRepository) {
//        this.chatRoomRepository = chatRoomRepository;
//        this.chatMessageRepository = chatMessageRepository;
//    }
//
//    // 특정 채팅방의 전체 메시지 내역 조회
//    public List<ChatMessage> getChatHistory(Long chatRoomId) {
//        return chatMessageRepository.findAllByChatRoom_IdOrderByCreatedAtAsc(chatRoomId);
//    }
//
//    // 1:1 채팅방 생성 (두 사용자)
//    public ChatRoom createChatRoom(User user1, User user2) {
//        Optional<ChatRoom> existing = chatRoomRepository.findChatRoomByUserIds(user1.getUserId(), user2.getUserId());
//        if (existing.isPresent()) {
//            return existing.get();
//        }
//        ChatRoom chatRoom = ChatRoom.create();
//        chatRoom.addMembers(user1, user2);
//        return chatRoomRepository.save(chatRoom);
//    }
//
//    // 채팅방 ID로 조회
//    public ChatRoom getChatRoomById(Long chatRoomId) {
//        return chatRoomRepository.findById(chatRoomId)
//                .orElseThrow(() -> new EntityNotFoundException("ChatRoom not found with id: " + chatRoomId));
//    }
//
//    // 사용자가 아직 나가지 않은(활성 상태인) 채팅방 전체 조회
//    public List<ChatRoom> getChatRoomsForUser(Long userId) {
//        return chatRoomRepository.findActiveChatRoomsByUserId(userId);
//    }
//
//    public void leaveChatRoom(Long chatRoomId, Long userId) {
//        ChatRoom chatRoom = getChatRoomById(chatRoomId);
//        boolean updated = false;
//        if (chatRoom.getUser1Id().equals(userId) && !chatRoom.isUser1Left()) {
//            chatRoom.setUser1Left(true);
//            updated = true;
//        } else if (chatRoom.getUser2Id().equals(userId) && !chatRoom.isUser2Left()) {
//            chatRoom.setUser2Left(true);
//            updated = true;
//        }
//        if (updated) {
//            // 두 사용자 모두 나갔다면 채팅방을 삭제합니다.
//            if (chatRoom.isUser1Left() && chatRoom.isUser2Left()) {
//                chatRoomRepository.delete(chatRoom);
//            } else {
//                chatRoomRepository.save(chatRoom);
//            }
//        }
//    }
//
//    // 채팅방 삭제 (두 사용자가 모두 나갔을 때 사용할 수 있음)
//    public void deleteChatRoom(Long chatRoomId) {
//        chatRoomRepository.deleteById(chatRoomId);
//    }
//}