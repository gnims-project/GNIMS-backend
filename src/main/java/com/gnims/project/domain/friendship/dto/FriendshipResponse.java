package com.gnims.project.domain.friendship.dto;

import com.gnims.project.domain.friendship.entity.FollowStatus;
import lombok.Getter;

@Getter
public class FriendshipResponse {
    private final Long followId;
    private final FollowStatus status;

    public FriendshipResponse(Long followId, FollowStatus followStatus) {
        this.followId = followId;
        this.status = followStatus;
    }

    public String receiveStatusMessage() {
        return status.getDescription();
    }

    public FriendShipCreatedEvent to(Long createBy, String senderName) {
        return new FriendShipCreatedEvent(createBy, senderName, this);
    }
}
