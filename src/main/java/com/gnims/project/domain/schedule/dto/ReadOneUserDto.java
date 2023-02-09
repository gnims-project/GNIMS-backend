package com.gnims.project.domain.schedule.dto;

import lombok.Getter;

@Getter
public class ReadOneUserDto {
    private String username;
    private String profileUri;

    public ReadOneUserDto(String username, String profileUri) {
        this.username = username;
        this.profileUri = profileUri;
    }
}
