package com.trip.planit.Chat.repository;

import com.trip.planit.Chat.entity.ChatParticipant;
import com.trip.planit.Chat.entity.ChatParticipant;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
  /** 특정 방의 아직 떠나지 않은(활성) 참가자 목록 조회 */
  List<ChatParticipant> findByUser_UserIdAndLeftAtIsNull(Long roomId);

  /** 특정 방과 사용자 이메일로 참가자 조회 */
  Optional<ChatParticipant> findByChatRoom_IdAndUser_Email(Long roomId, String email);

  List<ChatParticipant> findByChatRoom_IdAndLeftAtIsNull(Long roomId);

  @Transactional
  long deleteByLeftAtBefore(LocalDateTime threshold);
}