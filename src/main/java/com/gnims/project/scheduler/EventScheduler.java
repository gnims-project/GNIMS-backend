package com.gnims.project.scheduler;


import com.gnims.project.share.slack.SlackMessageSender;
import com.gnims.project.domain.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;

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
    private final SlackMessageSender slackMessageSender;

    /**
     * 매일 0시에 d-day 작업
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void updateEventDDay() {
        LocalDate today = LocalDate.now();
        try {
            eventRepository.updateDDay();
        } catch (Exception e) {
            slackMessageSender.sendTaskResult("[" + today + "] 디데이 감소 처리 중 오류가 발생했습니다. 관리자를 호출하십시오");
            log.info("[{} 디데이 감소 처리 중 오류가 발생했습니다]", today);
            throw new RuntimeException("디디에 처리 오류 발생");
        }
        slackMessageSender.sendTaskResult("[" + today + "] 그님스 데이터베이스에 보관중인 모든 Event의 D-day가 1 감소합니다.");

        log.info("[{} 디데이 처리가 완료되었습니다]", today);
    }
}
