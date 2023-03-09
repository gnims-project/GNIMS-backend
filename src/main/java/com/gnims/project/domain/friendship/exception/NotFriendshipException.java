package com.gnims.project.domain.friendship.exception;

import lombok.Getter;

@Getter
public class NotFriendshipException extends RuntimeException{
    public NotFriendshipException(String message) {
        super(message);
    }
}
