package com.gnims.project.domain.schedule.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Getter
public class EventOneQueryDto {
    private Long eventId;
    private LocalDate date;
    private LocalTime time;
    private String cardColor;
    private String subject;
    private String content;
    private Long dDay;
    private String username;

    public EventOneQueryDto(Long eventId, LocalDate date, LocalTime time, String cardColor, String subject, String content, String username) {
        this.eventId = eventId;
        this.date = date;
        this.time = time;
        this.cardColor = cardColor;
        this.subject = subject;
        this.content = content;
        this.dDay = calculateDDay();
        this.username = username;
    }

    private long calculateDDay() {
        return ChronoUnit.DAYS.between(LocalDate.now(), date);
    }
}
