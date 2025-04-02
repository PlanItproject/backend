package com.trip.planit.User.entity;

import jakarta.persistence.*;
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
