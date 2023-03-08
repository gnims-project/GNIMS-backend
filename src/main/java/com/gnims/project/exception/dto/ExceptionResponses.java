package com.gnims.project.exception.dto;

import lombok.Getter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Getter
public class ExceptionResponses {
    private Integer status;
    private List<String> messages;

    public ExceptionResponses(Integer status, List<String> messages) {
        this.status = status;
        Collections.sort(messages, Comparator.naturalOrder());
        this.messages = messages;
    }
}

