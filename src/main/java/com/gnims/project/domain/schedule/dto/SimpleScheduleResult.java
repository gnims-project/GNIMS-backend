package com.gnims.project.domain.schedule.dto;

import lombok.Getter;

@Getter
public class SimpleScheduleResult {
    private final Integer status;
    private final String message;

    public SimpleScheduleResult(Integer status, String message) {
        this.status = status;
        this.message = message;
    }
}
