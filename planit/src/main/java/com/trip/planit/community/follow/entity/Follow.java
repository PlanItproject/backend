package com.trip.planit.community.follow.entity;

import com.trip.planit.User.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "follower_id", nullable = false) // 팔로우를 한 사람
    private User follower;

    @ManyToOne
    @JoinColumn(name = "following_id", nullable = false) // 팔로우 당한 사람
    private User following;

    private LocalDateTime createdAt = LocalDateTime.now();


}
