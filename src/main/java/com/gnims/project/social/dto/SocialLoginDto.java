package com.gnims.project.social.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SocialLoginDto {

    private String message;
    private String nickname;
    private String email;

    public SocialLoginDto(String message, String nickname, String email) {
        this.message = message;
        this.nickname = nickname;
        this.email = email;
    }
}
