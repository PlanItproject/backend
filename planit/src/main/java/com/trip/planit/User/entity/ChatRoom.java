//package com.trip.planit.User.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.DynamicUpdate;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.jpa.domain.support.AuditingEntityListener;
//
//import java.time.LocalDateTime;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.UUID;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@Entity
//@Data
//@DynamicUpdate
//@EqualsAndHashCode(onlyExplicitlyIncluded = true)
//@EntityListeners(value = {AuditingEntityListener.class})
//@Table(name = "ChatRoom")
//public class ChatRoom {
//    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    @JoinTable(name = "ChatRoom_Members",
//            joinColumns = @JoinColumn(name = "chatRoomId"),
//            inverseJoinColumns = @JoinColumn(name = "userId"))
//    private Set<User> chatRoomMembers = new HashSet<>();
//
//    @EqualsAndHashCode.Include
//    @Id
//    @Column(name = "id")
//    private String id;
//
//    // 마지막 채팅 메시지 - (대화 목록에 미리보기)
//    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    @JoinColumn(name = "lastChatMesgId")
//    private ChatMessage lastChatMesg;
//
//    @Column(name = "createdAt", updatable = false)
//    @CreatedDate
//    private LocalDateTime createdAt;
//
//    public static ChatRoom create() {
//        ChatRoom room = new ChatRoom();
//        room.setId(UUID.randomUUID().toString());
//        return room;
//    }
//
//    public void addMembers(User roomMaker, User guest) {
//        this.chatRoomMembers.add(roomMaker);
//        this.chatRoomMembers.add(guest);
//    }
//}
