package com.gnims.project.domain.schedule.dto;


import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * jpql에서 Native Query 를 사용하기 위해서 필요한 DTO
 */

@Getter
public class ReadPendingResponse {
    private final Long eventId;
    private final String hostname;
    private final LocalDate date;
    private final LocalTime time;
    private final String cardColor;
    private final String subject;

    public ReadPendingResponse(Long eventId, String hostname, LocalDate date, LocalTime time, String cardColor, String subject) {
        this.eventId = eventId;
        this.hostname = hostname;
        this.date = date;
        this.time = time;
        this.cardColor = cardColor;
        this.subject = subject;
    }
}

