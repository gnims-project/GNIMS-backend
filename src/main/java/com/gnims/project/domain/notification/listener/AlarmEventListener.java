package com.gnims.project.domain.notification.listener;

import com.gnims.project.domain.friendship.dto.FriendShipCreatedEvent;
import com.gnims.project.domain.notification.dto.NotificationAllForm;
import com.gnims.project.domain.notification.dto.NotificationForm;
import com.gnims.project.domain.notification.dto.ReadNotificationResponse;
import com.gnims.project.domain.notification.entity.Notification;
import com.gnims.project.domain.notification.entity.NotificationType;
import com.gnims.project.domain.notification.repository.SseEmitterManager;
import com.gnims.project.domain.notification.service.NotificationService;
import com.gnims.project.domain.schedule.dto.ScheduleCreatedEvent;
import com.gnims.project.domain.schedule.dto.ScheduleInviteRepliedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

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
    public void processScheduleEvent(ScheduleCreatedEvent event) {
        //알림 만들기
        String message = event.getUsername() + "님께서 " + event.getSubject() + " 일정에 초대하셨습니다.";
        NotificationAllForm notificationForm = NotificationAllForm.of(event.getCreateBy(), event.getParticipantsId(), message, SCHEDULE);

        List<Notification> notifications = notificationService.createAll(notificationForm);
        //알림 보내기 - 클라이언트 측으로 보낼 정보
        sendToSse(SCHEDULE, notifications);
    }

    @Async
    @EventListener
    public void processFriendShipEvent(FriendShipCreatedEvent event) {
        //알림 만들기
        String message = event.getSenderName() + "님께서 팔로우하셨습니다.";
        NotificationForm notificationForm = NotificationForm.of(event.getCreateBy(), event.getFollowId(), message, FRIENDSHIP);
        Notification notification = notificationService.create(notificationForm);
        //알림 보내기 - 클라이언트 측으로 보낼 정보
        sendToSse(FRIENDSHIP, notification);
    }

    @Async
    @EventListener
    public void processScheduleSelectEvent(ScheduleInviteRepliedEvent event) {
        //알림 만들기
        String message = scheduleRepliedMessage(event);

        NotificationForm notificationForm = NotificationForm.of(event.getSenderId(), event.getReceiverId(), message, INVITE_RESPONSE);
        Notification notification = notificationService.create(notificationForm);
        sendToSse(INVITE_RESPONSE, notification);
    }

    @Nullable
    private static String scheduleRepliedMessage(ScheduleInviteRepliedEvent response) {
        String message = null;
        if (response.getScheduleStatus().equals(REJECT)) {
            message = response.getSenderName() + "님께서 " + response.getSubject() + " 일정을 거절하셨습니다.";
        }

        if (response.getScheduleStatus().equals(ACCEPT)) {
            message = response.getSenderName() + "님께서 " + response.getSubject() + " 일정을 수락하셨습니다.";
        }
        return message;
    }

    private void sendToSse(NotificationType notificationType, Notification notification) {
        SseEmitter sseEmitter = sseEmitterManager.getSseEmitters().get(notification.getUser().getId());
        try {
            ReadNotificationResponse notificationResponse = convert(notification);
            sseEmitterManager.send(sseEmitter, notificationType, notificationResponse);

        } catch (IOException | NullPointerException | IllegalStateException exception) {
            log.info("exception {} message {}", exception.getClass().getSimpleName(), exception.getMessage());
        }
    }

    private void sendToSse(NotificationType notificationType, List<Notification> notifications) {
        notifications.forEach(notification -> {
            SseEmitter sseEmitter = sseEmitterManager.getSseEmitters().get(notification.getUser().getId());
            ReadNotificationResponse notificationResponse = convert(notification);
            try {
                sseEmitterManager.send(sseEmitter, notificationType, notificationResponse);
            }
            catch (IOException | NullPointerException | IllegalStateException exception) {
                log.info("exception {} message {}", exception.getClass().getSimpleName(), exception.getMessage());
            }
        });
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
