package com.gnims.project.domain.schedule.dto;

import lombok.Getter;

@Getter
public class PageScheduleResult<T> {
    private final Integer status;
    private final String message;
    private final Integer totalPage;
    private final T data;

    public PageScheduleResult(Integer status, String message, Integer totalPage, T data) {
        this.status = status;
        this.message = message;
        this.totalPage = totalPage;
        this.data = data;
    }
}