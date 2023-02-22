package com.gnims.project.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SearchAllQueryDto {
    private Long userId;
    private String username;
    private String profileImage;
    private Boolean isFollowed;

    public SearchAllQueryDto(Long userId, String username, String profileImage, Boolean isFollowed) {
        this.userId = userId;
        this.username = username;
        this.profileImage = profileImage;
        this.isFollowed = isFollowed;
    }
}
