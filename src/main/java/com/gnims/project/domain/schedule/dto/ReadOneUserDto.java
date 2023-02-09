package com.gnims.project.domain.schedule.dto;

import lombok.Getter;

@Getter
public class ReadOneUserDto {
    private String username;

    public ReadOneUserDto(String username) {
        this.username = username;
    }
}
