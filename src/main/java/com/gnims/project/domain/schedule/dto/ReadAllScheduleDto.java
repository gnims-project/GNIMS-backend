package com.gnims.project.domain.schedule.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class ReadAllScheduleDto {
    private final Long eventId;
    private final LocalDate date;
    private final LocalTime time;
    private final String cardColor;
    private final String subject;
    private final Long dDay;
    private final String username;
    private final String profile;

    public ReadAllScheduleDto(Long eventId, LocalDate date, LocalTime time, String cardColor, String subject, Long dDay, String username, String profile) {
        this.eventId = eventId;
        this.date = date;
        this.time = time;
        this.cardColor = cardColor;
        this.subject = subject;
        this.dDay = dDay;
        this.username = username;
        this.profile = profile;
    }

    public boolean isSameEventId(Long eventId) {
        return this.eventId.equals(eventId);
    }
}
