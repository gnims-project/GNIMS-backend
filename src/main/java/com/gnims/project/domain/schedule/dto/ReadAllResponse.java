package com.gnims.project.domain.schedule.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
public class ReadAllResponse {
    private final Long eventId;
    private final LocalDate date;
    private final LocalTime time;
    private final String cardColor;
    private final String subject;
    private final Long dDay;
    private final List<ReadAllUserDto> invitees;

    public ReadAllResponse(Long eventId, LocalDate date, LocalTime time, String cardColor, String subject, Long dDay,
                           List<ReadAllUserDto> invitees) {
        this.eventId = eventId;
        this.date = date;
        this.time = time;
        this.cardColor = cardColor;
        this.subject = subject;
        this.dDay = dDay;
        this.invitees = invitees;
    }
}
