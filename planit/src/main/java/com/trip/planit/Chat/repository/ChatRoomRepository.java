package com.trip.planit.Chat.repository;

import com.trip.planit.Chat.entity.ChatRoom;
import com.trip.planit.Chat.entity.PrivateChatRoom;
import com.trip.planit.Chat.entity.RoomType;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // 수정: 실제 컬럼값으로만 검색
    List<ChatRoom> findByRoomTypeValue(String roomTypeValue);

    // 수정: enum 타입 편의 메서드만 default 로 제공
    default List<ChatRoom> findByRoomType(RoomType roomType) {
      return findByRoomTypeValue(roomType.name());
    }

    @Query("""
      SELECT r FROM PrivateChatRoom r
       JOIN r.participants p1
       JOIN r.participants p2
      WHERE p1.user.userId = :userAId
        AND p2.user.userId = :userBId
    """)
    Optional<PrivateChatRoom> findRoomByParticipantIds(
        @Param("userAId") Long userAId,
        @Param("userBId") Long userBId
    );
  }
