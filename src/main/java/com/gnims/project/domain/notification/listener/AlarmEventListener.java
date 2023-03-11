package com.gnims.project.domain.notification.listener;

import com.gnims.project.domain.friendship.dto.FriendShipServiceResponse;
import com.gnims.project.domain.notification.dto.NotificationForm;
import com.gnims.project.domain.notification.dto.ReadNotificationResponse;
import com.gnims.project.domain.notification.entity.Notification;
import com.gnims.project.domain.notification.entity.NotificationType;
import com.gnims.project.domain.notification.repository.SseEmitterManager;
import com.gnims.project.domain.notification.service.NotificationService;
import com.gnims.project.domain.schedule.dto.ScheduleServiceForm;
import com.gnims.project.domain.schedule.dto.ScheduleDecisionEventForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

import static com.gnims.project.domain.notification.entity.NotificationType.*;
import static com.gnims.project.domain.schedule.entity.ScheduleStatus.*;

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
            NotificationForm notificationForm = NotificationForm.of(form.getCreateBy(), participant, message, SCHEDULE);
            Notification notification = notificationService.create(notificationForm);
            //알림 보내기 - 클라이언트 측으로 보낼 정보
            process(SCHEDULE, notification);
        });
    }

    @Async
    @EventListener
    public void processFriendShipEvent(FriendShipServiceResponse response) {
        //알림 만들기
        String message = response.getSenderName() + "님께서 팔로우하셨습니다.";
        NotificationForm notificationForm = NotificationForm.of(response.getCreateBy(), response.getFollowId(), message, FRIENDSHIP);
        Notification notification = notificationService.create(notificationForm);
        //알림 보내기 - 클라이언트 측으로 보낼 정보
        process(FRIENDSHIP, notification);
    }

    @Async
    @EventListener
    public void processScheduleSelectEvent(ScheduleDecisionEventForm response) {
        //알림 만들기
        String message = decideMessage(response);

        NotificationForm notificationForm = NotificationForm.of(response.getSenderId(), response.getReceiverId(), message, INVITE_RESPONSE);
        Notification notification = notificationService.create(notificationForm);
        process(INVITE_RESPONSE, notification);
    }

    @Nullable
    private static String decideMessage(ScheduleDecisionEventForm response) {
        String message = null;
        if (response.getScheduleStatus().equals(REJECT)) {
            message = response.getSenderName() + "님께서 " + response.getSubject() + " 일정을 거절하셨습니다.";
        }

        if (response.getScheduleStatus().equals(ACCEPT)) {
            message = response.getSenderName() + "님께서 " + response.getSubject() + " 일정을 수락하셨습니다.";
        }
        return message;
    }

    private void process(NotificationType notificationType, Notification notification) {
        SseEmitter sseEmitter = sseEmitterManager.getSseEmitters().get(notification.getUser().getId());
        try {
            ReadNotificationResponse notificationResponse = convert(notification);
            sseEmitterManager.send(sseEmitter, notificationType, notificationResponse);

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
