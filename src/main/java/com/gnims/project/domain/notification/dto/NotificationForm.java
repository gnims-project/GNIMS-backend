package com.gnims.project.domain.notification.dto;

import com.gnims.project.domain.notification.entity.NotificationType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class NotificationForm extends NotificationAbstractForm {
    private final Long receiverId;

    public NotificationForm(Long createBy, String message, NotificationType notificationType, Long receiverId) {
        super(createBy, message, notificationType);
        this.receiverId = receiverId;
    }

    public static NotificationForm of(Long createBy, Long receiverId, String message, NotificationType notificationType) {
        return new NotificationForm(createBy, message, notificationType, receiverId);
    }
}
