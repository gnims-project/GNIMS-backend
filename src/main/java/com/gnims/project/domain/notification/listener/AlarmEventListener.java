package com.gnims.project.domain.notification.listener;

import com.gnims.project.domain.friendship.dto.FriendShipServiceResponse;
import com.gnims.project.domain.notification.entity.Notification;
import com.gnims.project.domain.notification.repository.SseEmitterManager;
import com.gnims.project.domain.notification.service.NotificationService;
import com.gnims.project.domain.schedule.dto.ScheduleServiceForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmEventListener {

    private final NotificationService notificationService;
    private final SseEmitterManager sseEmitterManager;
    /**
     * 비동기 더 공부해야되요. 쓰레드 풀이란걸 통해 관리해야 된다고 하네요?
     */
    @Async
    @EventListener
    public void scheduleCreatePush(ScheduleServiceForm form) {
        log.info("스케줄 등록 이벤트 리스너 작동");
        List<Long> participantsIds = form.getParticipantsId();
        sendScheduleAlarm(form, participantsIds);
    }

    @Async
    @EventListener
    public void friendshipPush(FriendShipServiceResponse response) {
        log.info("팔로우 이벤트 리스너 작동");
        sendFriendshipAlarm(response);
    }

    private void sendScheduleAlarm(ScheduleServiceForm form, List<Long> participantsIds) {

        for (Long participantsId : participantsIds) {
            Map<Long, SseEmitter> sseEmitters = sseEmitterManager.getSseEmitters();
            SseEmitter sseEmitter = sseEmitters.get(participantsId);
            try {
                String message = form.getUsername() + "님께서 " + form.getSubject() + " 일정에 초대하셨습니다.";
                log.info("이벤트 리스너 메시지 : {} TO USER ID : {}", message, participantsId);
                Notification notification = notificationService.createV2(participantsId, message);

                sseEmitter.send(SseEmitter.event()
                        .name("invite")
                        .data(notification, MediaType.APPLICATION_JSON));

            } catch (IOException e) {
                log.info("IO exception");
            } catch (NullPointerException e) {
                log.info("현재 {} 사용자는 알람을 사용하고 있지 않습니다.", participantsId);
            } catch (IllegalStateException e) {
                log.info("현재 {} 사용자의 Emitter는 꺼져있습니다.", participantsId);
            }
        }
    }

    private void sendFriendshipAlarm(FriendShipServiceResponse response) {

        Map<Long, SseEmitter> sseEmitters = sseEmitterManager.getSseEmitters();
        SseEmitter sseEmitter = sseEmitters.get(response.getFollowId());
        try {
            String message = response.getSenderName() + "님께서 팔로우하셨습니다.";

            log.info("이벤트 리스너 메시지 : {} TO USER ID : {}", message, response.getFollowId());
            Notification notification = notificationService.createV2(response.getFollowId(), message);

            sseEmitter.send(SseEmitter.event()
                    .name("follow")
                    .data(notification, MediaType.APPLICATION_JSON));

        } catch (IOException e) {
            log.info("IO exception");
        } catch (NullPointerException e) {
            log.info("현재 {} 사용자는 알람을 사용하고 있지 않습니다.");
        } catch (IllegalStateException e) {
            log.info("현재 {} 사용자의 Emitter는 꺼져있습니다.");
        }
    }

}
