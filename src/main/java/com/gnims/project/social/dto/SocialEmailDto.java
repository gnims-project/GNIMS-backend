package com.gnims.project.social.dto;

import lombok.Getter;

@Getter
public class SocialEmailDto {
    private String email;

    public SocialEmailDto(String email) {
        this.email = email;
    }
}
