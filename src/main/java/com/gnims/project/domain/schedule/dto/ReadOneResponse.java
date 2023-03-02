package com.gnims.project.domain.schedule.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
public class ReadOneResponse {
    private Long eventId;
    private LocalDate date;
    private LocalTime time;
    private String cardColor;
    private String subject;
    private String content;
    private Long hostId;
    private Long dDay;
    private List<ReadOneUserDto> invitees;

    public ReadOneResponse(Long eventId, LocalDate date, LocalTime time, String cardColor, String subject, String content, Long hostId, Long dDay, List<ReadOneUserDto> invitees) {
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
