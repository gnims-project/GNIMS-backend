package com.gnims.project.domain.user.dto;

import com.gnims.project.domain.user.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SearchResponseDto {
    private Long userId;
    private String nickname;
    private String profileImage;
    private Boolean isFollowed;

    public SearchResponseDto(User user, Boolean isFollowed) {
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.profileImage = user.getProfileImage();
        this.isFollowed = isFollowed;
    }
}
