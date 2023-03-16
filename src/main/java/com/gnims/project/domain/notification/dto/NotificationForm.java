package com.gnims.project.domain.notification.dto;

import com.gnims.project.domain.notification.entity.NotificationType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

import static com.gnims.project.domain.notification.entity.NotificationType.*;

@Slf4j
@Getter
public class NotificationForm<T> {
    private final Long createBy;
    private final T accepterId;
    private final String message;
    private final NotificationType notificationType;

    private NotificationForm(Long createBy, T accepterId, String message, NotificationType notificationType) {
        this.createBy = createBy;
        this.accepterId = accepterId;
        this.message = message;
        this.notificationType = notificationType;
    }

    public static NotificationForm of(Long createBy, Long accepterId, String message, NotificationType notificationType) {
        return new NotificationForm(createBy, accepterId, message,notificationType);
    }

    public static NotificationForm ofScheduleCreated(Long createBy, List<Long> accepterId, String message) {
        return new NotificationForm(createBy, accepterId, message, SCHEDULE);
    }

    public static NotificationForm ofFriendShipCreated(Long createBy, Long accepterId, String message) {
        return new NotificationForm(createBy, accepterId, message, FRIENDSHIP);
    }

    public Long getAccepterId() {

        return (Long) accepterId;
    }

    public List<Long> convertAccepterType() {
        log.info("타입 체크={}", accepterId.getClass());
        if (isList()) {
            log.info("형변환 진행합니다.");
            return (List<Long>) this.accepterId;
        }
        log.info("빈 리스트를 반환합니다.");
        return Collections.emptyList();
    }

    private boolean isList() {
        return this.accepterId instanceof List;
    }
}
