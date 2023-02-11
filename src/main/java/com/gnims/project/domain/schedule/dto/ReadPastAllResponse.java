package com.gnims.project.domain.schedule.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
public class ReadPastAllResponse {
    private Long eventId;
    private LocalDate date;
    private LocalTime time;
    private String cardColor;
    private String subject;
    private List<ReadAllUserDto> invitees;

    public ReadPastAllResponse(Long eventId, LocalDate date, LocalTime time, String cardColor, String subject, List<ReadAllUserDto> invitees) {
        this.eventId = eventId;
        this.date = date;
        this.time = time;
        this.cardColor = cardColor;
        this.subject = subject;
        this.invitees = invitees;
    }
}
