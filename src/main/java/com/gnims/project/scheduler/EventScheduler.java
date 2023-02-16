package com.gnims.project.scheduler;

import com.gnims.project.domain.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventScheduler {

    private final EventRepository eventRepository;

    /**
     * 매일 0시에 d-day 작업
     */
    @Scheduled(cron = "* * 0 * * *")
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
}
