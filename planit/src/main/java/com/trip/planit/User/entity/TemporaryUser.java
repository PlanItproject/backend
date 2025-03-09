package com.trip.planit.User.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", length = 20, nullable = false)
    private Platform platform;

    private LocalDateTime createdAt;

    // 이메일 인증 실패 횟수를 저장하는 필드 (기본값은 0)
    @Column(nullable = false)
    private int failedAttempts;

    // 자식 EmailVerification과의 관계 설정
    @OneToMany(mappedBy = "temporaryUserId", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<EmailVerification> emailVerifications;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false)
    private Language language;

}
