package com.gnims.project.domain.schedule.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
public class ReadAllResponse {
    private Long eventId;
    private LocalDate date;
    private LocalTime time;
    private String cardColor;
    private String subject;
    private Long dDay;
    private List<ReadAllUserDto> invitees;

    public ReadAllResponse(Long eventId, LocalDate date, LocalTime time, String cardColor, String subject, Long dDay, List<ReadAllUserDto> invitees) {
        this.eventId = eventId;
        this.date = date;
        this.time = time;
        this.cardColor = cardColor;
        this.subject = subject;
        this.dDay = dDay;
        this.invitees = invitees;
    }
}
