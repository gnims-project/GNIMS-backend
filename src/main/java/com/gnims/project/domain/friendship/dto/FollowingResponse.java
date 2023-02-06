package com.gnims.project.domain.friendship.dto;

import lombok.Getter;

@Getter
public class FollowingResponse {

    private Long followId;
    private String nickname;

    public FollowingResponse(Long followId, String nickname) {
        this.followId = followId;
        this.nickname = nickname;
    }
}
