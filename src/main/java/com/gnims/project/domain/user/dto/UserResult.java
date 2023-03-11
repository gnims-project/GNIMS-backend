package com.gnims.project.domain.user.dto;

import lombok.Getter;

@Getter
public class UserResult<T> {
    private final Integer status;
    private final String message;
    private final T data;

    public UserResult(Integer status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
