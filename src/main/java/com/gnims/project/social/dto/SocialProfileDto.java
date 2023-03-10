package com.gnims.project.social.dto;

import lombok.Getter;

@Getter
public class SocialProfileDto {
    private final String email;

    public SocialProfileDto(String email) {
        this.email = email;
    }
}