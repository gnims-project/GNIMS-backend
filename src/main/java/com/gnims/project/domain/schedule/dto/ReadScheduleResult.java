package com.gnims.project.domain.schedule.dto;

import lombok.Getter;

@Getter
public class ReadScheduleResult<T> {
    private Integer status;
    private String message;
    private T data;

    public ReadScheduleResult(Integer status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
