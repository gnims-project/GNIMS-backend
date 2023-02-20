package com.gnims.project.domain.friendship.dto;

import com.gnims.project.domain.friendship.entity.FollowStatus;
import lombok.Getter;

@Getter
public class FollowReadResponse {

    private Long followId;
    private String username;
    private String profile;
    private FollowStatus followStatus;


    public FollowReadResponse(Long followId, String username, String profile, FollowStatus followStatus) {
        this.followId = followId;
        this.username = username;
        this.profile = profile;
        this.followStatus = followStatus;
    }
}
