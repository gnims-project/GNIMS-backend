package com.gnims.project.domain.schedule.dto;

import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * id 는 스케줄을 생성한 사용자의 id를 의미합니다.
 */
@Getter
public class ScheduleServiceForm {
    private Long createBy;

    private String username;

    private LocalDate date;

    private LocalTime time;

    private String subject;

    private String content;

    private String cardColor;

    private List<Long> participantsId;

    public ScheduleServiceForm(Long id, String username, ScheduleForm scheduleForm) {
        this.createBy = id;
        this.username = username;
        this.date = scheduleForm.getDate();
        this.time = scheduleForm.getTime();
        this.subject = scheduleForm.getSubject();
        this.content = scheduleForm.getContent();
        this.cardColor = scheduleForm.getCardColor();
        this.participantsId = scheduleForm.getParticipantsId();
    }
}
