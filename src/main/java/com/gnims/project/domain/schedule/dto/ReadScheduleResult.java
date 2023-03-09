package com.gnims.project.domain.schedule.dto;

import lombok.Getter;

@Getter
public class ReadScheduleResult<T> {
    private final Integer status;
    private final String message;
    private final T data;

    public ReadScheduleResult(Integer status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
