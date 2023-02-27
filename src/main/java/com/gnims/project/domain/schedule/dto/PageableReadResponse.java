package com.gnims.project.domain.schedule.dto;

import lombok.Getter;

@Getter
public class PageableReadResponse<T> {
    private Integer size;
    private T data;

    public PageableReadResponse(Integer size, T data) {
        this.size = size;
        this.data = data;
    }
}
