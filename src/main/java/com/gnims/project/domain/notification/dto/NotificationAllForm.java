package com.gnims.project.domain.notification.dto;

import com.gnims.project.domain.notification.entity.NotificationType;
import lombok.Getter;

import java.util.List;

@Getter
public class NotificationAllForm extends NotificationAbstractForm{
    private final List<Long> receiversId;

    public NotificationAllForm(Long createBy, String message, NotificationType notificationType, List<Long> receiversId) {
        super(createBy, message, notificationType);
        this.receiversId = receiversId;
    }

    public static NotificationAllForm of(Long createBy, List<Long> receiversId, String message, NotificationType notificationType) {
        return new NotificationAllForm(createBy, message, notificationType, receiversId);
    }
}
