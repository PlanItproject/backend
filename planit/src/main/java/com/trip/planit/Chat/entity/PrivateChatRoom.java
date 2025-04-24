//package com.trip.planit.Chat.entity;
//
//import jakarta.persistence.*;
//import java.util.ArrayList;
//import java.util.List;
//import lombok.*;
//
//@Entity
//@DiscriminatorValue("PRIVATE")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class PrivateChatRoom extends ChatRoom {
//
//  @Id
//  @GeneratedValue(strategy = GenerationType.IDENTITY)
//  private Long id;
//
//  @OneToMany(mappedBy = "chatRoom",
//      cascade = CascadeType.ALL,
//      orphanRemoval = true)
//  private List<ChatParticipant> participants = new ArrayList<>();
//
//  @OneToMany(mappedBy = "chatRoom",
//      cascade = CascadeType.ALL,
//      orphanRemoval = true)
//  private List<ChatMessage> messages = new ArrayList<>();
//}
