//package com.trip.planit.Chat.repository;
//
//import com.trip.planit.Chat.entity.ChatMessage;
//import com.trip.planit.Chat.entity.PrivateChatRoom;
//import java.util.Optional;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.util.List;
//
//public interface PrivateChatRoomRepository extends JpaRepository<PrivateChatRoom, Long> {
//
//  /**
//   * 두 참가자 ID로 기존 1:1 채팅방이 있는지 조회
//   */
//  @Query("""
//        SELECT DISTINCT r
//          FROM PrivateChatRoom r
//          JOIN r.participants p1
//          JOIN r.participants p2
//         WHERE p1.user.userId = :userIdA
//           AND p2.user.userId = :userIdB
//    """)
//  Optional<PrivateChatRoom> findRoomByParticipantIds(
//      @Param("userIdA") Long userIdA,
//      @Param("userIdB") Long userIdB
//  );
//
//
//}
//
