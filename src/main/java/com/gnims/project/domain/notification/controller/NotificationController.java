package com.gnims.project.domain.notification.controller;

import com.gnims.project.domain.schedule.dto.ScheduleForm;
import com.gnims.project.domain.notification.repository.SseEmitterManager;
import com.gnims.project.security.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final SseEmitterManager sseEmitterManager;

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        SseEmitter sseEmitter = sseEmitterManager.save(userDetails.receiveUserId());
        sseEmitterManager.sendInitMessage(sseEmitter, userDetails.getUsername());

        return sseEmitter;
    }

    /**
     * 비동기 더 공부해야되요. 쓰레드 풀이란걸 통해 관리해야 된다고 하네요?
     */
    @Async
    @EventListener
    public void helloPush(ScheduleForm scheduleForm) {
        log.info("이벤트 리스너의 살기 감지!");
        List<Long> participantsIds = scheduleForm.getParticipantsId();
        sendAlarm(scheduleForm, participantsIds);
    }


    private void sendAlarm(ScheduleForm scheduleForm, List<Long> participantsIds) {

        for (Long participantsId : participantsIds) {
            Map<Long, SseEmitter> sseEmitters = sseEmitterManager.getSseEmitters();
            SseEmitter sseEmitter = sseEmitters.get(participantsId);
            try {
                log.info("이벤트 리스너 {}, {}", scheduleForm.getSubject(), scheduleForm.getParticipantsId());
                sseEmitter.send(scheduleForm, MediaType.APPLICATION_JSON);
                sseEmitter.send(SseEmitter.event()
                        .name("connect")
                        .data(scheduleForm.getSubject() + "에 초대하셨습니다."));

            } catch (IOException e) {
                log.info("IO exception");
            } catch (NullPointerException e) {
                log.info("현재 {} 사용자는 알람을 사용하고 있지 않습니다.", participantsId);
            } catch (IllegalStateException e) {
                log.info("현재 {} 사용자의 Emitter는 꺼져있습니다.", participantsId);
            }
        }
    }
}

