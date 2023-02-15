package com.gnims.project.domain.schedule.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Getter
public class EventAllQueryDto {
    private Long eventId;
    private LocalDate date;
    private LocalTime time;
    private String cardColor;
    private String subject;
    private Long dDay;
    private String username;
    private String profile;

    public EventAllQueryDto(Long eventId, LocalDate date, LocalTime time, String cardColor, String subject, String username, String profile) {
        this.eventId = eventId;
        this.date = date;
        this.time = time;
        this.cardColor = cardColor;
        this.subject = subject;
        this.dDay = calculateDDay();
        this.username = username;
        this.profile = profile;
    }

    private long calculateDDay() {
        return ChronoUnit.DAYS.between(LocalDate.now(), date);
    }
}
