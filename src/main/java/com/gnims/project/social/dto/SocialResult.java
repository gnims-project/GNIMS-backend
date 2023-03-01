package com.gnims.project.social.dto;

import lombok.Getter;

@Getter
public class SocialResult<T> {
    private Integer status;
    private String message;
    private T data;

    public SocialResult(Integer status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
