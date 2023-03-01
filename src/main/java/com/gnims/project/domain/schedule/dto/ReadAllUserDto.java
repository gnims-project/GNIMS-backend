package com.gnims.project.domain.schedule.dto;

import lombok.Getter;

@Getter
public class ReadAllUserDto {
    private String username;
    private String profile;

    public ReadAllUserDto(String username, String profile) {
        this.username = username;
        this.profile = profile;
    }
}
