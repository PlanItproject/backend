// ChatParticipant.java
package com.trip.planit.Chat.entity;

import com.fasterxml.jackson.annotation.JsonBackReference; // 수정: 순환참조 방지를 위해 추가
import com.trip.planit.User.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "chat_participant",
    uniqueConstraints = @UniqueConstraint(columnNames = {"chat_room_id", "user_id"}) // 수정: 동일한 방에 중복 참가 방지
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatParticipant {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // 오픈채팅을 위한 별명
  private String nickname;

  @Column(nullable = false, updatable = false)
  private LocalDateTime joinedAt;

  private LocalDateTime leftAt;

  @Column(name = "has_left", nullable = false)
  private boolean hasLeft = false;

  @ManyToOne(fetch = FetchType.LAZY, optional = false) // 수정: optional=false 로 변경
  @JoinColumn(name = "chat_room_id", nullable = false)
  @JsonBackReference // 수정: 순환참조 방지를 위해 추가
  private ChatRoom chatRoom;

  @PrePersist
  private void prePersist() {
    this.joinedAt = LocalDateTime.now(); // 수정: joinedAt 자동 세팅
  }

  /** 퇴장 helper */
  public void leave() {
    this.leftAt = LocalDateTime.now();
    this.hasLeft = true;
  }
}
