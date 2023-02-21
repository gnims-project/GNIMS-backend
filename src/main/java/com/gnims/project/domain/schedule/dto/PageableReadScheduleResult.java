package com.gnims.project.domain.schedule.dto;

import lombok.Getter;

@Getter
public class PageableReadScheduleResult<T> {
    private Integer status;
    private String message;
    private Integer totalPage;
    private T data;

    public PageableReadScheduleResult(Integer status, String message, Integer totalPage, T data) {
        this.status = status;
        this.message = message;
        this.totalPage = totalPage;
        this.data = data;
    }
}