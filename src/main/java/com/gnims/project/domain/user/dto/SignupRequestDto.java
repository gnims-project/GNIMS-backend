package com.gnims.project.domain.user.dto;

import lombok.Getter;

@Getter
public class SignupRequestDto {
    private String nickname;
    private String email;
    private String password;
}
