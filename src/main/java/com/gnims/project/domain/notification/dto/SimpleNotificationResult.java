package com.gnims.project.domain.notification.dto;

import lombok.Getter;

@Getter
public class SimpleNotificationResult {
    private Integer status;
    private String message;

    public SimpleNotificationResult(Integer status, String message) {
        this.status = status;
        this.message = message;
    }
}
