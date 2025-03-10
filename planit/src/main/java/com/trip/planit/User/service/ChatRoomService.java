//package com.trip.planit.User.service;
//
//import com.trip.planit.User.entity.ChatRoom;
//import com.trip.planit.User.entity.ChatMessage;
//import com.trip.planit.User.entity.User;
//import com.trip.planit.User.repository.ChatRoomRepository;
//import com.trip.planit.User.repository.ChatMessageRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import java.util.List;
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
//    // 사용자별 채팅방 목록 조회
//    public List<ChatRoom> getChatRoomsForUser(Long userId) {
//        return chatRoomRepository.findByChatRoomMembersUserId(userId);
//    }
//
//    // 특정 채팅방의 전체 메시지 내역 조회
//    public List<ChatMessage> getChatMessages(String chatRoomId) {
//        return chatMessageRepository.findByChatRoom_Id(chatRoomId);
//    }
//
//    // 1:1 채팅방 생성 (두 사용자)
//    public ChatRoom createChatRoom(User user1, User user2) {
//        ChatRoom chatRoom = ChatRoom.create();
//        chatRoom.addMembers(user1, user2);
//        return chatRoomRepository.save(chatRoom);
//    }
//}
