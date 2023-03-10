package com.gnims.project.social.dto;

import lombok.Getter;

@Getter
public class SocialEmailDto {
    private final String email;

    public SocialEmailDto(String email) {
        this.email = email;
    }
}
