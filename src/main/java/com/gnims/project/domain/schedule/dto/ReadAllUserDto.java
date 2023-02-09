package com.gnims.project.domain.schedule.dto;

import lombok.Getter;

@Getter
public class ReadAllUserDto {
    private String username;

    public ReadAllUserDto(String username) {
        this.username = username;
    }
}
