package com.gnims.project.domain.notification.dto;

import com.gnims.project.domain.notification.entity.NotificationType;
import lombok.Getter;

@Getter
public abstract class NotificationAbstractForm {
    private final Long createBy;
    private final String message;
    private final NotificationType notificationType;

    protected NotificationAbstractForm(Long createBy, String message, NotificationType notificationType) {
        this.createBy = createBy;
        this.message = message;
        this.notificationType = notificationType;
    }
}
