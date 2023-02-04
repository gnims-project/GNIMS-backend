package com.gnims.project.domain.user.dto;

import lombok.Getter;

@Getter
public class MessageResponseDto {
    private final String message;

    public MessageResponseDto(String message) {
        this.message = message;
    }
}
