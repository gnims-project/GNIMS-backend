package com.gnims.project.domain.schedule.dto;

import lombok.Getter;

@Getter
public class ReadAllUserDto {
    private final String username;
    private final String profile;

    public ReadAllUserDto(String username, String profile) {
        this.username = username;
        this.profile = profile;
    }
}
