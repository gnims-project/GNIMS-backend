package com.gnims.project.domain.notification.dto;

import lombok.Getter;

@Getter
public class ReadNotificationResult<T> {
    private Integer status;
    private String message;
    private T data;

    public ReadNotificationResult(Integer status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
