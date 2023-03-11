package com.gnims.project.domain.notification.entity;

import lombok.Getter;

@Getter
public enum NotificationType {
    SCHEDULE("invite"), FRIENDSHIP("follow"), INVITE_RESPONSE("invite_response");

    private final String eventType;

    NotificationType(String eventType) {
        this.eventType = eventType;
    }
}
