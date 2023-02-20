package com.gnims.project.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NicknameEmailDto {
    private String nickname;
    private String email;

    public NicknameEmailDto(String nickname, String email) {
        this.nickname = nickname;
        this.email = email;
    }
}
