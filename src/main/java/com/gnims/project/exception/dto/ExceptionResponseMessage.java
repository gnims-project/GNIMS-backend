package com.gnims.project.exception.dto;

import lombok.Getter;

@Getter
public class ExceptionResponseMessage {
    private Integer status;
    private String message;

    public ExceptionResponseMessage(Integer status, String message) {
        this.status = status;
        this.message = message;
    }
}
