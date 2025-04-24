package com.trip.planit.Chat.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference; // 수정: 순환참조 방지를 위해 추가
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Table(name = "chat_room")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "room_type", discriminatorType = DiscriminatorType.STRING)
@Getter @Setter @NoArgsConstructor
@DynamicInsert
@SuperBuilder
public abstract class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 메시지 기록
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ChatMessage> messages = new ArrayList<>();

    // 참가자 목록
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ChatParticipant> participants = new ArrayList<>();

    @Column(nullable=false, updatable=false)
    private LocalDateTime createdAt;

    @PrePersist protected void onCreate(){
        createdAt = LocalDateTime.now();
    }

    @Column(name="room_type", insertable=false, updatable=false)
    private String roomTypeValue;

    @Column(nullable = false)
    private Long creatorId;
}
