package com.gnims.project.domain.friendship.dto;

import lombok.Getter;

@Getter
public class FriendshipResult<T> {
    private final Integer status;
    private final String message;
    private final T data;

    public FriendshipResult(Integer status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
