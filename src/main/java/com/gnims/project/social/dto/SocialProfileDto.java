package com.gnims.project.social.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SocialProfileDto {
    private String email;

    public SocialProfileDto(String email) {
        this.email = email;
    }
}