package com.trip.planit.User.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "email_verification")
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "temporary_user_id", referencedColumnName = "temporary_user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private TemporaryUser temporaryUserId;

    @Column(nullable = false)
    private int verificationCode;

    @Column(nullable = false)
    private boolean verifiedEmail;

    @Column(nullable = false)
    private LocalDateTime createTime;

    @Column(nullable = false)
    private LocalDateTime expirationTime;
}

