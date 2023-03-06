package com.gnims.project.domain.user.dto;

import lombok.Getter;

@Getter
public class SearchResponseDto<T> {
    private Integer status;
    private String message;
    private T data;

    public SearchResponseDto(Integer status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
