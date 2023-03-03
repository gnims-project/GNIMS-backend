package com.gnims.project.scheduler;

import com.gnims.project.domain.notification.repository.SseEmitterManager;
import com.gnims.project.share.slack.SlackMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Emitter를 초기화하는 이유는 OOM 떄문입니다.
 * Emitter 초기화 작업은 트래픽에 따라 조절해야 하지만 그님스의 트래픽은 측정하기 힘들기 때문에
 * 매일 오전 4시에 한 번씩 초기화 작업을 수행해야 합니다.
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final SseEmitterManager sseEmitterManager;
    private final SlackMessageSender slackMessageSender;

    @Scheduled(cron = "0 0 4 * * *")
    public void clearEmitterContainer() {
        LocalDate today = LocalDate.now();
        try {
            sseEmitterManager.clear();
        } catch (Exception e) {
            slackMessageSender.sendTaskResult("[" + today + "] Emitter Map 초기화 중 오류가 발생했습니다. " +
                    "관리자를 호출하십시오");
            log.info("[{} Emitter 를 보관하는 자료 구조 초기화 중 오류가 발생했습니다]", today);
            throw new RuntimeException("Emitter 자료 구조 초기화 오류 발생");
        }
        slackMessageSender.sendTaskResult("[" + today + "] Emitter Map 초기화가 완료되었습니다.");

        log.info("[{} Emitter Map 초기화가 완료되었습니다]", today);
    }
}
