package com.trip.planit.User.repository;

import com.trip.planit.User.entity.EmailVerification;
import com.trip.planit.User.entity.TemporaryUser;
import com.trip.planit.User.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// 이메일 인증 코드를 데이터베이스에서 관리하는 리포지토리
@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    // 이메일로 최신 인증 기록을 가져오기
    Optional<EmailVerification> findTopByTemporaryUser_EmailOrderByCreateTimeDesc(String email);

    Optional<EmailVerification> findTopByTemporaryUserAndVerifiedEmailFalseOrderByCreateTimeDesc(TemporaryUser temporaryUser);

    void deleteByTemporaryUserId(Long temporaryUserId);

    void deleteByTemporaryUser_Email(String email);


}
