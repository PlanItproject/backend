package com.trip.planit.Chat.repository;

import com.trip.planit.Chat.entity.OpenChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OpenChatRoomRepository extends JpaRepository<OpenChatRoom, Long> {

  /**
   * 방장이 만든 모든 오픈채팅방 조회
   */
  List<OpenChatRoom> findByCreatorId(Long creatorId);

  /**
   * 특정 사용자가 아직 떠나지 않은(active) 오픈채팅방 조회
   */
  @Query("""
    SELECT r FROM OpenChatRoom r
    JOIN r.participants p
    WHERE p.user.userId = :userId
      AND p.leftAt IS NULL
  """)
  List<OpenChatRoom> findMyOpenRoomsByUserId(@Param("userId") Long userId);


  /**
   * 방 이름으로 단일 방 조회
   */
  Optional<OpenChatRoom> findByRoomName(String roomName);
}
