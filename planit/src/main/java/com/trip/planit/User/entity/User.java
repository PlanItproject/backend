package com.trip.planit.User.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long user_id;

    @Column(name = "loginId", length = 20, nullable = true, unique = true)
    private String loginId;

    @Column(name = "password", length = 255, nullable = true)
    private String password;

    @Column(name = "nickname", length = 20, nullable = false, unique = true)
    private String nickname;

    @Column(name="email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "profile")
    private String profile;     // 수정 필요함.

    @Enumerated(EnumType.STRING)
    @Column(name = "mbti", length = 20, nullable = false)
    private MBTI mbti;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", length = 20, nullable = false)
    private Platform platform;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", length = 20, nullable = false)
    private Language language;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;
}