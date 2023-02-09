package com.gnims.project.domain.schedule.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
public class ScheduleForm {
    private LocalDate date;
    private LocalTime time;
    private String subject;
    private String content;
    private String cardColor;

    private List<Long> participantsId;
}