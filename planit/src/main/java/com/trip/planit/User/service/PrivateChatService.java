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
public class PrivateChatService {

  private final ChatRoomRepository roomRepo;
  private final ChatMessageRepository msgRepo;

  @Autowired
  public PrivateChatService(ChatRoomRepository roomRepo, ChatMessageRepository msgRepo) {
    this.roomRepo = roomRepo;
    this.msgRepo = msgRepo;
  }

  /** 1:1 채팅방 생성/조회 */
  public ChatRoom createOrGetRoom(User u1, User u2) {
    Optional<ChatRoom> existing = roomRepo.findChatRoomByUserIds(u1.getUserId(), u2.getUserId());
    if (existing.isPresent())
      return existing.get();

    ChatRoom room = ChatRoom.create();
    room.addMembers(u1, u2);

    return roomRepo.save(room);
  }

  /** 메시지 저장 */
  public ChatMessage saveMessage(ChatMessage message) {
    return msgRepo.save(message);
  }


  /** 메시지 읽음 처리 */
  public void markAsRead(Long messageId) {
    msgRepo.findById(messageId).ifPresent(m -> {
      m.setRead(true);
      msgRepo.save(m);
    });
  }

  /** 두 사용자 간 전체 1:1 채팅 기록 조회 */
  public List<ChatMessage> getUserChatHistory(String user1, String user2) {
    return msgRepo.findChatHistory(user1, user2);
  }

  /** 사용자별 활성 방 조회 */
  public List<ChatRoom> getActiveRooms(Long userId) {
    return roomRepo.findActiveChatRoomsByUserId(userId);
  }

  /** 방 나가기 */
  public void leaveRoom(Long roomId, Long userId) {
    ChatRoom room = roomRepo.findById(roomId)
        .orElseThrow(() -> new EntityNotFoundException("Room not found"));
    if (room.getUser1Id().equals(userId)) room.setUser1Left(true);
    else if (room.getUser2Id().equals(userId)) room.setUser2Left(true);
    // 두 명 다 나가면 삭제, 아니면 저장
    if (room.isUser1Left() && room.isUser2Left()) roomRepo.delete(room);
    else roomRepo.save(room);
  }
}
