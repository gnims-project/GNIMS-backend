package com.gnims.project.domain.schedule.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

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

    public EventOneQueryDto(Long eventId, LocalDate date, LocalTime time, String cardColor, String subject, String content, Long dDay, String username) {
        this.eventId = eventId;
        this.date = date;
        this.time = time;
        this.cardColor = cardColor;
        this.subject = subject;
        this.content = content;
        this.dDay = dDay;
        this.username = username;
    }
}
