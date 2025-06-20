package com.trip.planit.Chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("OPEN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OpenChatRoom extends ChatRoom {

  @Column(nullable = false)
  private String roomName;

//  @Column(nullable = false)
//  private Long creatorId;
}
