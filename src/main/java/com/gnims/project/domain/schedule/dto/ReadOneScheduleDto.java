package com.gnims.project.domain.schedule.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class ReadOneScheduleDto {
    private final Long eventId;
    private final LocalDate date;
    private final LocalTime time;
    private final String cardColor;
    private final String subject;
    private final String content;
    private final Long hostId;
    private final Long dDay;
    private final String username;

    public ReadOneScheduleDto(Long eventId, LocalDate date, LocalTime time, String cardColor, String subject,
                              String content, Long hostId, Long dDay, String username) {
        this.eventId = eventId;
        this.date = date;
        this.time = time;
        this.cardColor = cardColor;
        this.subject = subject;
        this.content = content;
        this.hostId = hostId;
        this.dDay = dDay;
        this.username = username;
    }

    public boolean isCreatedBy(Long userId) {
        return this.hostId.equals(userId);
    }
}
