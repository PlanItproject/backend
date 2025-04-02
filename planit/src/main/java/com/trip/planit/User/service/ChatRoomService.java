package com.trip.planit.User.service;

import com.trip.planit.User.entity.ChatRoom;
import com.trip.planit.User.entity.ChatMessage;
import com.trip.planit.User.entity.User;
import com.trip.planit.User.repository.ChatRoomRepository;
import com.trip.planit.User.repository.ChatMessageRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Autowired
    public ChatRoomService(ChatRoomRepository chatRoomRepository, ChatMessageRepository chatMessageRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    // 특정 채팅방의 전체 메시지 내역 조회
    public List<ChatMessage> getChatHistory(Long chatRoomId) {
        return chatMessageRepository.findAllByChatRoom_IdOrderByCreatedAtAsc(chatRoomId);
    }

    // 1:1 채팅방 생성 (두 사용자)
    public ChatRoom createChatRoom(User user1, User user2) {
        Optional<ChatRoom> existing = chatRoomRepository.findChatRoomByUserIds(user1.getUserId(), user2.getUserId());
        if (existing.isPresent()) {
            return existing.get();
        }
        ChatRoom chatRoom = ChatRoom.create();
        chatRoom.addMembers(user1, user2);
        return chatRoomRepository.save(chatRoom);
    }

    // 채팅방 ID로 조회
    public ChatRoom getChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("ChatRoom not found with id: " + chatRoomId));
    }

    // 사용자가 포함된 채팅방 전체 조회 (user1Id 또는 user2Id에 해당)
    public List<ChatRoom> getChatRoomsForUser(Long userId) {
        return chatRoomRepository.findByUser1IdOrUser2Id(userId, userId);
    }
}
