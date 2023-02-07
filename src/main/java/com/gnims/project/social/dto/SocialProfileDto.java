package com.gnims.project.social.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SocialProfileDto {
    private String id;
    private String nickname;
    private String email;

    public SocialProfileDto(String id, String nickname, String email) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
    }
}