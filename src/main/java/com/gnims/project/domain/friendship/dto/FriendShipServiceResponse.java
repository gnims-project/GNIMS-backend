package com.gnims.project.domain.friendship.dto;

import com.gnims.project.domain.friendship.entity.FollowStatus;
import lombok.Getter;

@Getter
public class FriendShipServiceResponse {
    private final Long createBy;
    private final String senderName;
    private final Long followId;
    private final FollowStatus status;

    public FriendShipServiceResponse(Long createBy, String senderName, FriendshipResponse response) {
        this.createBy = createBy;
        this.senderName = senderName;
        this.followId = response.getFollowId();
        this.status = response.getStatus();
    }
}
