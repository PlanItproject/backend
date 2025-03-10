//package com.trip.planit.User.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import org.springframework.cglib.core.Local;
//import org.springframework.data.annotation.CreatedDate;
//
//import java.time.LocalDate;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@Entity
//@Data
//@Table(name = "ChatMessage")
//public class ChatMessage {
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "chat_room_id")
//    private ChatRoom chatRoom;
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private MessageType type;
//
//    @Column(name = "content")
//    private String content;
//
//    @Column(name = "sender")
//    private String sender;
//
//    @Column(name = "receiver")
//    private String receiver;
//
//    @Column(name = "createdAt", updatable = false)
//    @CreatedDate
//    private LocalDate createdAt;
//
//    @Column(name = "is_read")
//    private boolean isRead = false;
//}
