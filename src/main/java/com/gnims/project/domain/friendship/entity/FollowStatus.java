package com.gnims.project.domain.friendship.entity;

import lombok.Getter;

@Getter
public enum FollowStatus {
    INIT("첫 팔로우"),ACTIVE("팔로우"), INACTIVE("팔로우 취소");

    private final String description;

    FollowStatus(String description) {
        this.description = description;
    }
}
