package com.trip.planit.User.repository;

import com.trip.planit.User.entity.EmailVerification;
import com.trip.planit.User.entity.TemporaryUser;
import com.trip.planit.User.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// 이메일 인증 코드를 데이터베이스에서 관리하는 리포지토리
@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    // 이메일로 최신 인증 기록 조회
    Optional<EmailVerification> findTopByTemporaryUserId_EmailOrderByCreateTimeDesc(String email);

    // 인증이 완료되지 않은 최신 인증 기록 조회
    Optional<EmailVerification> findTopByTemporaryUserIdAndVerifiedEmailFalseOrderByCreateTimeDesc(TemporaryUser temporaryUser);

    // TemporaryUser의 id로 삭제 (내부 TemporaryUser 엔티티의 id 속성은 temporaryUserId)
    void deleteByTemporaryUserId_TemporaryUserId(Long temporaryUserId);

    // 이메일로 삭제
    void deleteByTemporaryUserId_Email(String email);

    Optional<EmailVerification> findByUserEmail(@Param("email") String email);
    Optional<EmailVerification> findByTemporaryUserId_Email(String email);


}
