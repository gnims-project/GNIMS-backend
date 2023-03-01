package com.gnims.project.domain.notification.dto;

import com.gnims.project.domain.notification.entity.NotificationType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReadAllNotificationResponse {
    private Long notificationId;
    private LocalDateTime dateTime;
    private Long createBy;
    private String message;
    private boolean isChecked;
    private NotificationType notificationType;

    public ReadAllNotificationResponse(Long notificationId, LocalDateTime dateTime, Long createBy,
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
