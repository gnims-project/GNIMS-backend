package com.gnims.project.domain.schedule.dto;

import lombok.Getter;

@Getter
public class PageableReadResponse<T> {
    private Integer totalPage;
    private T data;

    public PageableReadResponse(Integer totalPage, T data) {
        this.totalPage = totalPage;
        this.data = data;
    }
}
