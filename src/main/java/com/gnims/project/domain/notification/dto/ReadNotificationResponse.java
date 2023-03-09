package com.gnims.project.domain.notification.dto;

import com.gnims.project.domain.notification.entity.NotificationType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReadNotificationResponse {
    private final Long notificationId;
    private final LocalDateTime dateTime;
    private final Long createBy;
    private final String message;
    private final boolean isChecked;
    private final NotificationType notificationType;

    public ReadNotificationResponse(Long notificationId, LocalDateTime dateTime, Long createBy,
                                    String message, boolean isChecked, NotificationType notificationType) {
        this.notificationId = notificationId;
        this.dateTime = dateTime;
        this.createBy = createBy;
        this.message = message;
        this.isChecked = isChecked;
        this.notificationType = notificationType;
    }

    public boolean getIsChecked() {
        return isChecked;
    }
}
