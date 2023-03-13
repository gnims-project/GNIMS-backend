package com.gnims.project.domain.user.dto;

import lombok.Getter;

@Getter
public class SimpleMessageResult {
    private final Integer status;
    private final String message;

    public SimpleMessageResult(Integer status, String message) {
        this.status = status;
        this.message = message;
    }
}
