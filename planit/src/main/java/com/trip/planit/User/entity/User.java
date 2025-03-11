package com.trip.planit.User.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Data
@Table(name = "user", indexes = {
        @Index(name = "idx_email", columnList = "email")
})

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name="isGoogleLogin", nullable = false)
    private boolean isGoogleLogin = false;

    @Column(name="email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "password", length = 255, nullable = true)
    private String password;

    @Column(name = "nickname", length = 20, nullable = false, unique = true)
    private String nickname;

    @Column(name = "profile")
    private String profile;

    @Enumerated(EnumType.STRING)
    @Column(name = "mbti", length = 20, nullable = false)
    private MBTI mbti;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", length = 20, nullable = false)
    private Platform platform;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false)
    private Language language;

    // 이메일 인증 실패 횟수를 저장하는 필드 (기본값은 0)
    @Column(nullable = false)
    private int failedAttempts;

    private LocalDateTime deletionScheduledAt; // 예약된 삭제 시각 저장.

    // 탈퇴 사유 1~4
    @Column(name="deletereason")
    private DeleteReason deleteReason;

    // 탈퇴 사유 기타
    @Column(name="deleteReason_Description")
    private String deleteReason_Description;
}