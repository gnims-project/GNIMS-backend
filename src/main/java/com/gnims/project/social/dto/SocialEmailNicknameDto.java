package com.gnims.project.social.dto;

import lombok.Getter;

@Getter
public class SocialEmailNicknameDto {

    private String email;
    private String nickname;

    public SocialEmailNicknameDto(String email, String nickname) {
        this.email = email;
        this.nickname = nickname;
    }
}
