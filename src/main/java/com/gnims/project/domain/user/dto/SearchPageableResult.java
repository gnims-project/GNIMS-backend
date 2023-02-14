package com.gnims.project.domain.user.dto;

import com.gnims.project.domain.friendship.dto.PagingDataResponse;
import lombok.Getter;

@Getter
public class SearchPageableResult<T, PAGE> {
    private Integer status;
    private String message;
    private PAGE page;
    private T data;

    public SearchPageableResult(Integer status, String message, PagingDataResponse response) {
        this.status = status;
        this.message = message;
        this.page = (PAGE) response.getPage();
        this.data = (T) response.getData();
    }
}
