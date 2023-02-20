package com.gnims.project.domain.friendship.dto;

import lombok.Getter;

@Getter
public class FollowReadResponse {

    private Long followId;
    private String username;
    private String profile;

    public FollowReadResponse(Long followId, String username) {
        this.followId = followId;
        this.username = username;
    }

    public FollowReadResponse(Long followId, String username, String profile) {
        this.followId = followId;
        this.username = username;
        this.profile = profile;
    }
}
