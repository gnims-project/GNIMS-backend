package com.gnims.project.domain.notification.listener;

import com.gnims.project.domain.friendship.dto.FriendShipServiceResponse;
import com.gnims.project.domain.notification.dto.NotificationForm;
import com.gnims.project.domain.notification.dto.ReadNotificationResponse;
import com.gnims.project.domain.notification.entity.Notification;
import com.gnims.project.domain.notification.repository.SseEmitterManager;
import com.gnims.project.domain.notification.service.NotificationService;
import com.gnims.project.domain.schedule.dto.ScheduleServiceForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

import static com.gnims.project.domain.notification.entity.NotificationType.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmEventListener {

    private final NotificationService notificationService;
    private final SseEmitterManager sseEmitterManager;

    @Async
    @EventListener
    public void processScheduleEvent(ScheduleServiceForm form) {
        //알림 만들기
        String message = form.getUsername() + "님께서 " + form.getSubject() + " 일정에 초대하셨습니다.";

        form.getParticipantsId().forEach(participant -> {
            NotificationForm notificationForm = new NotificationForm(form.getCreateBy(), participant, message, SCHEDULE);
            Notification notification = notificationService.create(notificationForm);
            //알림 보내기 - 클라이언트 측으로 보낼 정보
            process("invite", notification);
        });
    }

    @Async
    @EventListener
    public void processFriendShipEvent(FriendShipServiceResponse response) {
        //알림 만들기
        String message = response.getSenderName() + "님께서 팔로우하셨습니다.";
        NotificationForm notificationForm = new NotificationForm(response.getCreateBy(), response.getFollowId(), message, FRIENDSHIP);
        Notification notification = notificationService.create(notificationForm);
        //알림 보내기 - 클라이언트 측으로 보낼 정보
        process("follow", notification);
    }

    private void process(String eventType, Notification notification) {
        SseEmitter sseEmitter = sseEmitterManager.getSseEmitters().get(notification.getUser().getId());
        try {
            ReadNotificationResponse notificationResponse = convert(notification);
            sseEmitterManager.send(sseEmitter, eventType, notificationResponse);

        } catch (IOException | NullPointerException | IllegalStateException exception) {
            log.info("exception {} message {}", exception.getClass().getSimpleName(), exception.getMessage());
        }
    }

    private ReadNotificationResponse convert(Notification notification) {
        return new ReadNotificationResponse(
                notification.getId(),
                notification.getCreateAt(),
                notification.getCreateBy(),
                notification.getMessage(),
                notification.getIsChecked(),
                notification.getNotificationType());
    }

}
