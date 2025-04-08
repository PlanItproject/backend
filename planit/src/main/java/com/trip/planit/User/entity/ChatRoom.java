package com.trip.planit.User.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "chat_room")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 예시: 두 사용자의 이메일을 저장
    private Long user1Id;
    private Long user2Id;

    private LocalDateTime createdAt;

    // 채팅 메시지와 1:N 관계 설정 (Cascade, orphanRemoval 옵션 포함)
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ChatMessage> messages = new ArrayList<>();

    // /** 수정됨 **/ : 각 사용자가 채팅방을 나갔는지 여부 (false: 아직 남아있음)
    @Column(name = "user1_left", nullable = false)
    private boolean user1Left = false;

    @Column(name = "user2_left", nullable = false)
    private boolean user2Left = false;

    public static ChatRoom create() {
        ChatRoom room = new ChatRoom();
        room.setCreatedAt(LocalDateTime.now());
        return room;
    }

    // 두 사용자의 ID를 정렬해서 저장 (순서가 달라도 같은 방으로 인식하기 위해)
    public void addMembers(User user1, User user2) {
        if (user1.getUserId() < user2.getUserId()){
            this.user1Id = user1.getUserId();
            this.user2Id = user2.getUserId();
        } else {
            this.user1Id = user2.getUserId();
            this.user2Id = user1.getUserId();
        }
    }
}
