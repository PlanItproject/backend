package com.trip.planit.Chat.service;

import com.trip.planit.Chat.entity.ChatMessage;
import com.trip.planit.Chat.entity.ChatParticipant;
import com.trip.planit.Chat.entity.ChatRoom;
import com.trip.planit.Chat.entity.MessageType;
import com.trip.planit.Chat.entity.RoomType;
import com.trip.planit.Chat.repository.ChatMessageRepository;
import com.trip.planit.Chat.repository.ChatParticipantRepository;
import com.trip.planit.Chat.repository.ChatRoomRepository;
import com.trip.planit.Chat.entity.OpenChatRoom;
import com.trip.planit.Chat.repository.OpenChatRoomRepository;
import com.trip.planit.User.apiPayload.code.status.ErrorStatus;
import com.trip.planit.User.apiPayload.exception.GeneralException;
import com.trip.planit.User.entity.User;
import com.trip.planit.User.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OpenChatService {

  private final ChatRoomRepository chatRoomRepository;
  private final ChatParticipantRepository chatParticipantRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final UserRepository userRepository;
  private final OpenChatRoomRepository openChatRoomRepository;

  @Autowired
  public OpenChatService(
      ChatRoomRepository chatRoomRepository,
      ChatParticipantRepository chatParticipantRepository,
      ChatMessageRepository chatMessageRepository,
      UserRepository userRepository, OpenChatRoomRepository openChatRoomRepository) {
    this.chatRoomRepository = chatRoomRepository;
    this.chatParticipantRepository = chatParticipantRepository;
    this.chatMessageRepository = chatMessageRepository;
    this.userRepository = userRepository;
    this.openChatRoomRepository = openChatRoomRepository;
  }

  /** 오픈채팅방 생성 + 방장 참가 */
  @Transactional
  public OpenChatRoom createOpenRoom(Long creatorId, String roomName) {
    User creator = userRepository.findById(creatorId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

    OpenChatRoom room = OpenChatRoom.builder()
        .roomName(roomName)
        .creatorId(creatorId)
        .build();
    room.setCreatedAt(LocalDateTime.now());
    room = (OpenChatRoom) chatRoomRepository.save(room);

    ChatParticipant admin = ChatParticipant.builder()
        .chatRoom(room)
        .user(creator)
        .nickname("방장")
        .joinedAt(LocalDateTime.now())
        .build();
    chatParticipantRepository.save(admin);

    return room;
  }
  /** 방 목록 조회 */
  public List<ChatRoom> listRooms() {
    return chatRoomRepository.findByRoomType(RoomType.OPEN);
  }

  /** 방 참가 **/
  public ChatParticipant joinRoom(Long roomId, Long userId, String nickname) {
    if ("방장".equals(nickname)) {
      throw new GeneralException(ErrorStatus.BAD_REQUEST_CUSTOM);    }

    OpenChatRoom room = (OpenChatRoom) chatRoomRepository.findById(roomId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.ROOM_NOT_FOUND));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

    ChatParticipant p = ChatParticipant.builder()
        .chatRoom(room)
        .user(user)
        .nickname(nickname)
        .joinedAt(LocalDateTime.now())
        .build();
    return chatParticipantRepository.save(p);
  }

  /** 특정 방 내 특정 유저 참여 정보 */
  public ChatParticipant getParticipant(Long roomId, String userEmail) {
    return chatParticipantRepository.findByChatRoom_IdAndUser_Email(roomId, userEmail)
        .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_IN_ROOM));
  }

  /** 참가자 퇴장 처리 (leftAt 세팅) */
  public ChatParticipant leaveRoom(Long roomId, String userEmail) {
    ChatParticipant p = chatParticipantRepository.findByChatRoom_IdAndUser_Email(roomId, userEmail)
        .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_IN_ROOM));
    p.setLeftAt(LocalDateTime.now());  // 수정: leftAt 세팅
    return chatParticipantRepository.save(p);
  }

  /** 메세지 저장 */
  @Transactional
  public ChatMessage saveMessage(ChatMessage incoming) {
    // 1) DB에서 실제 ChatRoom 엔티티 로드
    ChatRoom room = chatRoomRepository.findById(
        incoming.getChatRoom().getId()
    ).orElseThrow(() ->
        new GeneralException(ErrorStatus.ROOM_NOT_FOUND));

    // 2) 새 ChatMessage 인스턴스에 필요한 값 복사
    ChatMessage msg = ChatMessage.builder()
        .chatRoom(room)
        .type(incoming.getType())
        .content(incoming.getContent())
        .senderId(incoming.getSenderId())
        .sender(incoming.getSender())
        .receiverId(incoming.getReceiverId())
        .receiver(incoming.getReceiver())
        .build();

    // 3) 저장 (PrePersist가 createdAt 세팅)
    return chatMessageRepository.save(msg);
  }


  /** 강퇴: 방장만 가능, 삭제 대신 leftAt 세팅 */
  @Transactional
  public void kick(Long roomId, String targetNickname, Long requesterId) {
    // 1) 방 조회 & 타입 체크
    ChatRoom room = getOpenChatRoomOrThrow(roomId);

    // 2) 요청자가 방장인지 확인
    OpenChatRoom openRoom = (OpenChatRoom) room;
    if (!openRoom.getCreatorId().equals(requesterId)) {
      throw new GeneralException(ErrorStatus.NOT_ROOM_OWNER);
    }

    // 3) 강퇴 대상 조회 (leftAt 가 null 인 사람 중에서)
    ChatParticipant target = room.getParticipants().stream()
        .filter(p -> p.getNickname().equals(targetNickname) && p.getLeftAt() == null)
        .findFirst()
        .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_IN_ROOM));

    // DB 삭제 방식 ㄴㄴ
    target.setLeftAt(LocalDateTime.now());
    chatParticipantRepository.save(target);
  }

  /** 내가 떠나지 않은(퇴장하지 않은) 방만 조회 */
  public List<ChatRoom> getMyRooms(Long userId) {
    // 수정: ChatParticipant에서 leftAt이 NULL인 것만 조회 후 방 매핑
    return chatParticipantRepository.findByUser_UserIdAndLeftAtIsNull(userId).stream()
        .map(ChatParticipant::getChatRoom)
        .toList();
  }

  public List<String> getNicknamesInRoom(Long roomId) {
    return chatParticipantRepository.findByChatRoom_IdAndLeftAtIsNull(roomId)
        .stream()
        .map(ChatParticipant::getNickname)
        .toList();
  }

  public OpenChatRoom getOpenChatRoomOrThrow(Long roomId) {
    ChatRoom room = chatRoomRepository.findById(roomId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.ROOM_NOT_FOUND));

    if (!(room instanceof OpenChatRoom openRoom)) {
      throw new GeneralException(ErrorStatus.BAD_REQUEST_CUSTOM);
    }

    return openRoom;
  }
  /** 내가 떠나지 않은 오픈 채팅방만 OpenChatRoom으로 조회 */
  public List<OpenChatRoom> getMyOpenRooms(Long userId) {
    return openChatRoomRepository.findMyOpenRoomsByUserId(userId);
  }

  /** 시스템 메시지 빌더 (DB 저장 전에 ChatMessage 엔티티로 만듭니다) */
  @Transactional(readOnly = true)
  public ChatMessage buildSystemMessage(Long roomId, Long senderId, String sender, String content) {
    ChatRoom room = chatRoomRepository.findById(roomId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.ROOM_NOT_FOUND));
    return ChatMessage.builder()
        .chatRoom(room)
        .type(MessageType.SYSTEM)
        .senderId(senderId)
        .sender(sender)
        .content(content)
        .build();
  }

  public List<ChatMessage> getMessages(Long roomId) {
    return chatMessageRepository.findAllByChatRoomIdOrderByCreatedAtAsc(roomId);
  }

}
