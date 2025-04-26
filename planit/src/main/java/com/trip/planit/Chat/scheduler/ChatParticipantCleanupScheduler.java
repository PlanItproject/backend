package com.trip.planit.Chat.scheduler;

import com.trip.planit.Chat.repository.ChatParticipantRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatParticipantCleanupScheduler {

  private final ChatParticipantRepository participantRepo;

  /**
   * 매달 1일 오전 9시 실행 (cron: 초 분 시 일 월 요일)
   */
  @Scheduled(cron = "0 0 9 1 * *")
  public void cleanUpOldParticipants() {
    LocalDateTime threshold = LocalDateTime.now().minusMonths(1);
    long deleted = participantRepo.deleteByLeftAtBefore(threshold);
    log.info("ChatParticipantCleanupScheduler: deleted {} participants with leftAt <= {}", deleted, threshold);
  }
}
