package com.gnims.project.domain.notification.dto;

import com.gnims.project.domain.notification.entity.NotificationType;
import lombok.Getter;

import java.util.List;

import static com.gnims.project.domain.notification.entity.NotificationType.*;

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
}
