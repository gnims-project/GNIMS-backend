package com.gnims.project.domain.friendship.dto;

import com.gnims.project.domain.friendship.entity.FollowStatus;
import lombok.Getter;

@Getter
public class FollowResponse {

    private Long followId;
    private FollowStatus status;

    public FollowResponse(Long followId, FollowStatus status) {
        this.followId = followId;
        this.status = status;
    }

    public String receiveFollowStatus() {
        return status.getDescription();
    }
}
