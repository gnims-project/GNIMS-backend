package com.gnims.project.exception.dto;

import lombok.Getter;

@Getter
public class AuthenticationCodeValidityException extends Exception {

    public AuthenticationCodeValidityException(String message) {
        super(message);
    }
}
