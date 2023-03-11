package com.gnims.project.domain.user.dto;

import lombok.Getter;

@Getter
public class SearchAllQueryDto {
    private final Long userId;
    private final String username;
    private final String profileImage;
    private final Boolean isFollowed;

    public SearchAllQueryDto(Long userId, String username, String profileImage, Boolean isFollowed) {
        this.userId = userId;
        this.username = username;
        this.profileImage = profileImage;
        this.isFollowed = isFollowed;
    }
}
