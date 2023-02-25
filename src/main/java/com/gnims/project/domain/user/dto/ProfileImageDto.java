package com.gnims.project.domain.user.dto;

import lombok.Getter;

@Getter
public class ProfileImageDto {

    private String profileImage;

    public ProfileImageDto(String profileImage) {
        this.profileImage = profileImage;
    }
}
