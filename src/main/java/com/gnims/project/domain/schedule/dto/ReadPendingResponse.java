package com.gnims.project.domain.schedule.dto;


import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * jpql에서 Native Query 를 사용하기 위해서 필요한 DTO
 */

@Getter
public class ReadPendingResponse {
    private Long eventId;
    private String hostname;
    private LocalDate date;
    private LocalTime time;
    private String cardColor;
    private String subject;

    public ReadPendingResponse(Long eventId, String hostname, LocalDate date, LocalTime time, String cardColor, String subject) {
        this.eventId = eventId;
        this.hostname = hostname;
        this.date = date;
        this.time = time;
        this.cardColor = cardColor;
        this.subject = subject;
    }
}

