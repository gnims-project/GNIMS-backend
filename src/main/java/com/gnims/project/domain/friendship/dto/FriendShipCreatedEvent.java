package com.gnims.project.domain.friendship.dto;

import com.gnims.project.domain.friendship.entity.FollowStatus;
import lombok.Getter;

@Getter
public class FriendShipCreatedEvent {
    private final Long createBy;
    private final String senderName;
    private final Long followId;
    private final FollowStatus status;

    public FriendShipCreatedEvent(Long createBy, String senderName, FriendshipResponse response) {
        this.createBy = createBy;
        this.senderName = senderName;
        this.followId = response.getFollowId();
        this.status = response.getStatus();
    }
}
