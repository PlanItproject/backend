package com.trip.planit.Chat.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.trip.planit.Chat.entity.MessageType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Data
@Table(name = "ChatMessage")
public class ChatMessage {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    @JsonBackReference  // 직렬화
    private ChatRoom chatRoom;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private MessageType type;

    @Column(name = "content")
    private String content;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "sender")
    private String sender;

    @Column(name = "receiver_id", nullable = true)
    private Long receiverId;

    @Column(name = "receiver", nullable = true)
    private String receiver;

    @Column(name = "createdAt", updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "is_read")
    private boolean isRead = false;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
