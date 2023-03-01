package com.gnims.project.domain.notification.dto;

import com.gnims.project.domain.notification.entity.NotificationType;
import lombok.Getter;

@Getter
public class ReadAllNotificationResponse {
    private Long notificationId;
    private Long createBy;
    private String message;
    private boolean isChecked;
    private NotificationType notificationType;

    public ReadAllNotificationResponse(Long notificationId, Long createBy, String message,
                                       boolean isChecked, NotificationType notificationType) {
        this.notificationId = notificationId;
        this.createBy = createBy;
        this.message = message;
        this.isChecked = isChecked;
        this.notificationType = notificationType;
    }

    public boolean getIsChecked() {
        return isChecked;
    }
}
