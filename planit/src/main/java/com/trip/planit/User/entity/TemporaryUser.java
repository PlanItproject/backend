package com.trip.planit.User.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "temporary_user")
public class TemporaryUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "temporary_user_id")
    private Long temporaryUserId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    // 회원가입 로직 완료하면 필요없는 entity 지우기.

    @Column(name = "profile")
    private String profile;

//    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
    private MBTI mbti;

    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", length = 20, nullable = false)
    private Platform platform;

    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
    private Language language;

    private LocalDateTime createdAt;

}
