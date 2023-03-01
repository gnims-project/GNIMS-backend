package com.gnims.project.domain.user.dto;

import com.gnims.project.domain.user.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SearchResponseDto {
    private Long userId;
    private String username;
    private String profileImage;
    private Boolean isFollowed;

    public SearchResponseDto(User user, Boolean isFollowed) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.profileImage = user.getProfileImage();
        this.isFollowed = isFollowed;
    }
}
