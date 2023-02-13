package com.gnims.project.domain.user.dto;

import lombok.Getter;

@Getter
public class LoginResponseDto {
    private String nickname;
    private String email;
    private String profileImage;

    public LoginResponseDto(String nickname, String email, String profileImage) {
        this.nickname = nickname;
        this.email = email;
        this.profileImage = profileImage;
    }
}
