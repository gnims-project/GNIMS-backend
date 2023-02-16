package com.gnims.project.domain.notification.dto;

import com.gnims.project.domain.notification.entity.AlarmType;
import com.gnims.project.domain.notification.entity.Notification;
import com.gnims.project.domain.notification.util.AlarmTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NotificationResponseDto {

    private Long notificationId;
    private String message;
    private Long articlesId;
    private Boolean readStatus;
    private AlarmType alarmType;
    private String title;
    private String createdAt;


    @Builder
    public NotificationResponseDto(Long id, String message, Long articlesId, Boolean readStatus,
                                   AlarmType alarmType, String title, String createdAt) {
        this.notificationId = id;
        this.message = message;
        this.articlesId = articlesId;
        this.readStatus = readStatus;
        this.title = title;
        this.alarmType = alarmType;
        this.createdAt = createdAt;
    }

    public static NotificationResponseDto create(Notification notification) {
        String createdAt = AlarmTime.times(notification.getCreateAt());

        return NotificationResponseDto.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .alarmType(notification.getAlarmType())
                .articlesId(notification.getUrl())
                .title(notification.getTitle())
                .readStatus(notification.getReadState())
                .createdAt(createdAt)
                .build();
    }
}
