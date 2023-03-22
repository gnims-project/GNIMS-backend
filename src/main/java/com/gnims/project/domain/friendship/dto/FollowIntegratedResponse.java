package com.gnims.project.domain.friendship.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class FollowIntegratedResponse {
    private final List<FollowReadResponse> follow;
    private final Integer count;

    private FollowIntegratedResponse(List<FollowReadResponse> follow, Integer count) {
        this.follow = follow;
        this.count = count;
    }

    public static FollowIntegratedResponse of(List<FollowReadResponse> follow, Integer count) {
        return new FollowIntegratedResponse(follow, count);
    }
}
