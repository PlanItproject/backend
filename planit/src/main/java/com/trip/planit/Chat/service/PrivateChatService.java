package com.trip.planit.Chat.service;

import com.trip.planit.Chat.dto.PrivateChatRoomRequest;
import com.trip.planit.Chat.dto.PrivateChatRoomResponse;
import com.trip.planit.Chat.entity.ChatParticipant;
import com.trip.planit.Chat.entity.ChatRoom;
import com.trip.planit.Chat.entity.ChatMessage;
import com.trip.planit.Chat.entity.PrivateChatRoom;
import com.trip.planit.User.apiPayload.code.status.ErrorStatus;
import com.trip.planit.User.apiPayload.exception.GeneralException;
import com.trip.planit.User.entity.User;
import com.trip.planit.Chat.repository.ChatParticipantRepository;
import com.trip.planit.Chat.repository.ChatRoomRepository;
import com.trip.planit.Chat.repository.ChatMessageRepository;
import com.trip.planit.Chat.repository.PrivateChatRoomRepository;
import com.trip.planit.User.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PrivateChatService {

  private final ChatRoomRepository roomRepo;
  private final ChatMessageRepository msgRepo;
  private final PrivateChatRoomRepository privateChatRoomRepository;
  private final ChatParticipantRepository chatParticipantRepository;
  private final UserRepository userRepo;

  @Autowired
  public PrivateChatService(
      ChatRoomRepository roomRepo,
      ChatMessageRepository msgRepo,
      PrivateChatRoomRepository privateChatRoomRepository,
      ChatParticipantRepository chatParticipantRepository,
      UserRepository userRepo
  ) {
    this.roomRepo = roomRepo;
    this.msgRepo = msgRepo;
    this.privateChatRoomRepository = privateChatRoomRepository;
    this.chatParticipantRepository = chatParticipantRepository;
    this.userRepo = userRepo;
  }

  /** 1:1 채팅방 생성 또는 기존 방 조회 */
  @Transactional
  public PrivateChatRoomResponse createChatRoom(
      String requesterEmail,
      PrivateChatRoomRequest request
  ) {
    // 1) 사용자 조회
    User me    = userRepo.findByEmail(requesterEmail)
        .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
    User guest = userRepo.findById(request.getGuestId())
        .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

    // 2) 방 생성·조회 (비즈니스 로직)
    PrivateChatRoom room = createOrGetRoom(me.getUserId(), guest.getUserId());

    // 3) 마지막 메시지 조회
    ChatMessage last = msgRepo
        .findFirstByChatRoomIdOrderByCreatedAtDesc(room.getId())
        .orElse(null);

    // 4) 응답 DTO 변환
    return PrivateChatRoomResponse.builder()
        .chatRoomId(room.getId())
        .roomMakerId(me.getUserId())
        .roomMakerNickname(me.getNickname())
        .guestId(guest.getUserId())
        .guestNickname(guest.getNickname())
        .otherNickname(guest.getNickname())
        .lastMessage(last != null ? last.getContent() : "")
        .lastMessageTime(last != null
            ? last.getCreatedAt().toString()
            : "")
        .build();
  }

  // 기존 로직: 방 생성 또는 조회
  public PrivateChatRoom createOrGetRoom(Long userAId, Long userBId) {
    // 1) 기존 방 조회
    Optional<PrivateChatRoom> existing =
        privateChatRoomRepository.findRoomByParticipantIds(userAId, userBId);
    if (existing.isPresent()) {
      return existing.get();
    }

    // 2) 새 방 생성 — creatorId 필수 세팅
    PrivateChatRoom room = new PrivateChatRoom();
    room.setCreatorId(userAId);  // ← 이 한 줄이 빠져 있었음!

    // (Optional) 방 생성 시간 기록 필드가 있다면 같이 세팅
    // room.setCreatedAt(LocalDateTime.now());

    User userA = userRepo.findById(userAId)
        .orElseThrow(() -> new EntityNotFoundException("User A not found"));
    User userB = userRepo.findById(userBId)
        .orElseThrow(() -> new EntityNotFoundException("User B not found"));

    room.getParticipants().add(ChatParticipant.builder()
        .chatRoom(room)
        .user(userA)
        .nickname(userA.getNickname())
        .joinedAt(LocalDateTime.now())
        .build());
    room.getParticipants().add(ChatParticipant.builder()
        .chatRoom(room)
        .user(userB)
        .nickname(userB.getNickname())
        .joinedAt(LocalDateTime.now())
        .build());

    // 3) 저장
    return privateChatRoomRepository.save(room);
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
    return msgRepo.findPrivateChatHistory(user1, user2);
  }

  /** 사용자별 활성 방 조회 (아직 leave 하지 않은 방) */
  public List<ChatRoom> getActiveRooms(Long userId) {
    return chatParticipantRepository
        .findByUser_UserIdAndLeftAtIsNull(userId)
        .stream()
        .map(ChatParticipant::getChatRoom)
        .toList();
  }

  /** 방 나가기 (leftAt 세팅) 및 남은 참가자 없으면 방 삭제 */
  public void leaveRoom(Long roomId, Long userId) {
    // 1) 참가자 조회 및 leftAt 세팅
    ChatParticipant p = chatParticipantRepository
        .findByChatRoom_IdAndUser_UserId(roomId, userId)
        .orElseThrow(() -> new EntityNotFoundException("Participant not found"));
    p.setLeftAt(LocalDateTime.now());
    chatParticipantRepository.save(p);

    // 2) 남아 있는(active) 참가자 확인
    boolean hasActive = chatParticipantRepository
        .existsByChatRoom_IdAndLeftAtIsNull(roomId);

    // 3) 만약 active 참가자 없으면 방 삭제
    if (!hasActive) {
      roomRepo.deleteById(roomId);
    }
  }

  /** 1:1 채팅 메시지 저장 */
  public ChatMessage saveMessage(ChatMessage message) {
    Long roomId = message.getChatRoom().getId();
    PrivateChatRoom room = privateChatRoomRepository.findById(roomId)
        .orElseThrow(() -> new EntityNotFoundException("PrivateChatRoom not found: " + roomId));

    message.setChatRoom(room);
    ChatMessage saved = msgRepo.save(message);
    room.getMessages().add(saved);
    return saved;
  }
}
