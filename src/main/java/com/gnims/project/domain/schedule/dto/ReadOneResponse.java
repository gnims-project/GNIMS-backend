package com.gnims.project.domain.schedule.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
public class ReadOneResponse {
    private final Long eventId;
    private final LocalDate date;
    private final LocalTime time;
    private final String cardColor;
    private final String subject;
    private final String content;
    private final Long hostId;
    private final Long dDay;
    private final List<ReadOneUserDto> invitees;

    public ReadOneResponse(Long eventId, LocalDate date, LocalTime time, String cardColor, String subject, String content,
                           Long hostId, Long dDay, List<ReadOneUserDto> invitees) {
        this.eventId = eventId;
        this.date = date;
        this.time = time;
        this.cardColor = cardColor;
        this.subject = subject;
        this.content = content;
        this.hostId = hostId;
        this.dDay = dDay;
        this.invitees = invitees;
    }
}
