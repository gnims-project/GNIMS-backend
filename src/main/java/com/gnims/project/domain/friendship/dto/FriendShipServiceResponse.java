package com.gnims.project.domain.friendship.dto;

import com.gnims.project.domain.friendship.entity.FollowStatus;
import lombok.Getter;

@Getter
public class FriendShipServiceResponse {
    private String senderName;
    private Long followId;
    private FollowStatus status;

    public FriendShipServiceResponse(String senderName, FriendshipResponse response) {
        this.senderName = senderName;
        this.followId = response.getFollowId();
        this.status = response.getStatus();
    }
}
