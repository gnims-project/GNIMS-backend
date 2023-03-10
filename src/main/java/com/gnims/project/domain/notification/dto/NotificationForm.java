package com.gnims.project.domain.notification.dto;

import com.gnims.project.domain.notification.entity.NotificationType;
import lombok.Getter;

@Getter
public class NotificationForm {
    private final Long createBy;
    private final Long accepterId;
    private final String message;
    private final NotificationType notificationType;

    private NotificationForm(Long createBy, Long accepterId, String message, NotificationType notificationType) {
        this.createBy = createBy;
        this.accepterId = accepterId;
        this.message = message;
        this.notificationType = notificationType;
    }

    public static NotificationForm of(Long createBy, Long accepterId, String message, NotificationType notificationType) {
        return new NotificationForm(createBy, accepterId, message,notificationType);
    }
}
