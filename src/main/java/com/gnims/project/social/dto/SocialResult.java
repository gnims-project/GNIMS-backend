package com.gnims.project.social.dto;

import lombok.Getter;

@Getter
public class SocialResult<T> {
    private final Integer status;
    private final String message;
    private final T data;

    public SocialResult(Integer status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
