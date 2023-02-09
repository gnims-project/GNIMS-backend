package com.gnims.project.domain.schedule.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
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

    public ReadAllResponse(Long eventId, LocalDate date, LocalTime time, String cardColor, String subject, List<ReadAllUserDto> invitees) {
        this.eventId = eventId;
        this.date = date;
        this.time = time;
        this.cardColor = cardColor;
        this.subject = subject;
        this.dDay = calculateDDay();
        this.invitees = invitees;
    }

    private long calculateDDay() {
        return ChronoUnit.DAYS.between(LocalDate.now(), date);
    }
}
