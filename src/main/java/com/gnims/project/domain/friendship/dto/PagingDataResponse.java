package com.gnims.project.domain.friendship.dto;

import lombok.Getter;

@Getter
public class PagingDataResponse<T, PAGE> {
    private PAGE page;
    private T data;

    public PagingDataResponse(PAGE page, T data) {
        this.page = page;
        this.data = data;
    }
}
