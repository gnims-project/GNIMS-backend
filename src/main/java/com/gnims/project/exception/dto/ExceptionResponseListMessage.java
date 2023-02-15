package com.gnims.project.exception.dto;

import lombok.Getter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Getter
public class ExceptionResponseListMessage {
    private Integer status;
    private List<String> messages;

    public ExceptionResponseListMessage(Integer status, List<String> messages) {
        this.status = status;
        Collections.sort(messages, Comparator.naturalOrder());
        this.messages = messages;
    }
}

