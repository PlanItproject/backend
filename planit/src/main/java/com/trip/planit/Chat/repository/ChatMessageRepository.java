package com.trip.planit.Chat.repository;

import com.trip.planit.Chat.entity.ChatMessage;
import com.trip.planit.Chat.entity.RoomType;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 1:1 프라이빗 채팅 기록
    @Query(
        "SELECT m FROM ChatMessage m " +
            " WHERE m.chatRoom.roomTypeValue = 'PRIVATE' " +
            "   AND ((m.sender   = :user1 AND m.receiver = :user2) " +
            "     OR (m.sender   = :user2 AND m.receiver = :user1)) " +
            " ORDER BY m.createdAt ASC"
    )
    List<ChatMessage> findPrivateChatHistory(
        @Param("user1") String user1,
        @Param("user2") String user2
    );

    List<ChatMessage> findAllByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);
}
