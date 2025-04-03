package com.trip.planit.User.repository;

import com.trip.planit.User.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByUser1IdAndUser2Id(long user1Id, long user2Id);

    List<ChatRoom> findByUser1IdOrUser2Id(Long user1Id, Long user2Id);

    // 두 사용자 ID가 순서와 상관없이 일치하는 채팅방 조회
    @Query("SELECT c FROM ChatRoom c WHERE (c.user1Id = :userId1 AND c.user2Id = :userId2) " +
            "OR (c.user1Id = :userId2 AND c.user2Id = :userId1)")
    Optional<ChatRoom> findChatRoomByUserIds(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    // /** 수정됨 **/ : 특정 사용자가 아직 나가지 않은(활성 상태인) 채팅방 전체 조회
    @Query("SELECT c FROM ChatRoom c WHERE ((c.user1Id = :userId AND c.user1Left = false) OR (c.user2Id = :userId AND c.user2Left = false))")
    List<ChatRoom> findActiveChatRoomsByUserId(@Param("userId") Long userId);
}
