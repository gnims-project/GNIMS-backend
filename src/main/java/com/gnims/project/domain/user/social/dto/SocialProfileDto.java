package com.gnims.project.domain.user.social.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SocialProfileDto {
    private Long id;
    private String nickname;
    private String email;

    public SocialProfileDto(Long id, String nickname, String email) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
    }
}
