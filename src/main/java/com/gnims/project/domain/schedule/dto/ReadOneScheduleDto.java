package com.gnims.project.domain.schedule.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class ReadOneScheduleDto {
    private Long eventId;
    private LocalDate date;
    private LocalTime time;
    private String cardColor;
    private String subject;
    private String content;
    private Long hostId;
    private Long dDay;
    private String username;

    public ReadOneScheduleDto(Long eventId, LocalDate date, LocalTime time, String cardColor, String subject, String content, Long hostId, Long dDay, String username) {
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
