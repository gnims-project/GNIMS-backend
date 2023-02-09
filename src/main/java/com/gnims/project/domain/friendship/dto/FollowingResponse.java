package com.gnims.project.domain.friendship.dto;

import lombok.Getter;

@Getter
public class FollowingResponse {

    private Long followId;
    private String username;

    public FollowingResponse(Long followId, String username) {
        this.followId = followId;
        this.username = username;
    }
}
