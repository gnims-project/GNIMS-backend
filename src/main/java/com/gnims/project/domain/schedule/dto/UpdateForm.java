package com.gnims.project.domain.schedule.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class UpdateForm {
    private String cardColor;
    private String subject;
    private String content;
    private LocalDate date;
    private LocalTime time;

}
