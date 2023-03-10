package com.gnims.project.domain.user.dto;

import com.gnims.project.domain.user.entity.User;
import lombok.Getter;

@Getter
public class LoginResponseDto {
    private final Long userId;
    private final String nickname;
    private final String email;
    private final String profileImage;

    public LoginResponseDto(User user) {
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.email = user.makePureEmail();
        this.profileImage = user.getProfileImage();
    }
}
