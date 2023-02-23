package com.gnims.project.domain.friendship.dto;

import com.gnims.project.domain.friendship.entity.FollowStatus;
import lombok.Getter;

@Getter
public class FriendshipResponse {

    private Long followId;
    private FollowStatus status;

    public FriendshipResponse(Long followId, FollowStatus followStatus) {
        this.followId = followId;
        this.status = followStatus;
    }

    public String receiveStatusMessage() {
        return status.getDescription();
    }

    public boolean isStatus(FollowStatus followStatus) {
        return this.status.equals(followStatus);
    }

    public FriendShipServiceResponse convertServiceResponse(String senderName) {
        return new FriendShipServiceResponse(senderName, this);
    }
}
