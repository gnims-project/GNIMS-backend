package com.gnims.project.exception;

import lombok.Getter;

@Getter
public class NotFoundInformationException extends RuntimeException {

    public NotFoundInformationException(String message) {
        super(message);
    }
}
