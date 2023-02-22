package com.gnims.project.scheduler;

import com.gnims.project.domain.event.repository.EventRepository;
import com.gnims.project.share.gmail.EmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 2023-02-16 기준, 현재 그님스는 단일 인스턴스 입니다.
 * 인스턴스가 추가될 경우 인스턴스마다 scheduler 작동됩니다.
 * 인스턴스를 추가 계획이 있다면 먼저 scheduler lock 을 설정해야합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventScheduler {

    private final EventRepository eventRepository;
    private final EmailRepository emailRepository;

    /**
     * 매일 0시에 d-day 작업
     */
    @Scheduled(cron = "0 0 0 * * *")
    private void updateEventDDay() {

        try {
            eventRepository.updateDDay();
        }
        catch (Exception e) {
            log.info("[디데이 처리 중 오류가 발생했습니다]");
            throw new RuntimeException("디디에 처리 오류 발생");
        }
        log.info("[디데이 처리가 완료되었습니다]");
    }

    /**
     * DB 메일 테이블 비우기
     */
    @Scheduled(cron = "0 0 0 * * *")
    private void deleteAuthMail() {

        try {
            emailRepository.deleteByCreateAtBefore(LocalDateTime.now().minusMinutes(183));
        }
        catch (Exception e) {
            log.info("[인증 메일 삭제 중 오류가 발생했습니다]");
            throw new RuntimeException("인증 메일 처리 오류 발생");
        }
        log.info("[인증 메일 처리가 완료되었습니다]");
    }
}
