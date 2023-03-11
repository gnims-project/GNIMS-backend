package com.gnims.project.scheduler;

import com.gnims.project.share.email.EmailRepository;
import com.gnims.project.share.slack.SlackMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 2023-02-16 기준, 현재 그님스는 단일 인스턴스 입니다.
 * 인스턴스가 추가될 경우 인스턴스마다 scheduler 작동됩니다.
 * 인스턴스를 추가 계획이 있다면 먼저 scheduler lock 을 설정해야합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailScheduler {

    private final EmailRepository emailRepository;
    private final SlackMessageSender slackMessageSender;

    /**
     * DB 메일 테이블 비우기
     */
    @Scheduled(cron = "10 0 0 * * *")
    public void deleteAuthMail() {
        LocalDate today = LocalDate.now();
        try {
            emailRepository.deleteAuthEmail(LocalDateTime.now().minusMinutes(183));
        }
        catch (Exception e) {
            slackMessageSender.sendTaskResult("["+ today + "] 인증 메일 삭제 중 오류가 발생했습니다. 관리자를 호출하십시오");
            log.info("[인증 메일 삭제 중 오류가 발생했습니다]");
            throw new RuntimeException("인증 메일 처리 오류 발생");
        }
        slackMessageSender.sendTaskResult("[" + today + "] " + today.minusDays(1).getMonthValue()
                + "월 " + today.minusDays(1).getDayOfMonth() + "일 20:57:10 이전에 생성된 인증 메일이 삭제됩니다.");
                
        log.info("[인증 메일 삭제 처리가 완료되었습니다]");
    }
}

