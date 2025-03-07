package com.trip.planit.User.service;

import java.time.LocalDateTime;
import java.util.List;

import com.trip.planit.User.entity.TemporaryUser;
import com.trip.planit.User.repository.TemporaryUserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TemporaryUserCleanupScheduler {

    private final TemporaryUserRepository temporaryUserRepository;

    public TemporaryUserCleanupScheduler(TemporaryUserRepository temporaryUserRepository) {
        this.temporaryUserRepository = temporaryUserRepository;
    }

    // 1분마다 실행하여 10분 이상 지난 임시 사용자 삭제
    @Scheduled(fixedDelay = 60000)
    public void cleanupExpiredTemporaryUsers() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(15);
        List<TemporaryUser> expiredUsers = temporaryUserRepository.findByCreatedAtBefore(expirationTime);
        if (!expiredUsers.isEmpty()) {
            temporaryUserRepository.deleteAll(expiredUsers);
            System.out.println("Deleted " + expiredUsers.size() + " expired temporary users.");
        }
    }
}
