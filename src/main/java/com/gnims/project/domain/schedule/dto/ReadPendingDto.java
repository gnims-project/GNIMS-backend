package com.gnims.project.domain.schedule.dto;


import java.time.LocalDate;
import java.time.LocalTime;

/**
 * jpql에서 Native Query 를 사용하기 위해서 필요한 DTO
 */

public interface ReadPendingDto {
    String getUsername();
    Long getEvent_Id();
    LocalDate getDate();
    LocalTime getTime();
    String getCard_Color();
    String getSubject();

}

