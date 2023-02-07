package com.gnims.project.domain.friendship.dto;

import lombok.Getter;

/**
 * 컨트롤러 단에서 반환값을 객체로 랩핑하는 용도입니다.
 */
@Getter
public class FriendshipPageableResult<T, PAGE> {
    private Integer status;
    private String message;
    private PAGE page;
    private T data;

    public FriendshipPageableResult(Integer status, String message, PagingDataResponse response) {
        this.status = status;
        this.message = message;
        this.page = (PAGE) response.getPage();
        this.data = (T) response.getData();
    }
}
