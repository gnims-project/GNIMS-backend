package com.gnims.project.domain.user.dto;

import com.gnims.project.domain.user.entity.SocialCode;
import lombok.Getter;

@Getter
public class LoginRequestDto {
    private SocialCode socialCode;
    private String email;
    private String password;
}
