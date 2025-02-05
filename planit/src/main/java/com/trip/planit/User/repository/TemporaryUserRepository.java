package com.trip.planit.User.repository;

import com.trip.planit.User.entity.TemporaryUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TemporaryUserRepository extends JpaRepository<TemporaryUser, Long> {
    Optional<TemporaryUser> findByEmail(String email); // 이메일로 임시 사용자 조회
    void deleteByEmail(String email);
}
