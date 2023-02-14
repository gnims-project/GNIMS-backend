package com.gnims.project.domain.user.dto;

import lombok.Getter;

@Getter
public class LoginResponseDto {
    private String nickname;
    private String email;

    public LoginResponseDto(String nickname, String email) {
        this.nickname = nickname;
        this.email = email;
    }
}
