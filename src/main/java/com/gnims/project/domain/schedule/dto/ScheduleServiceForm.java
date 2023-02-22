package com.gnims.project.domain.schedule.dto;

import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
public class ScheduleServiceForm {
    private Long id;

    private String username;

    private LocalDate date;

    private LocalTime time;

    private String subject;

    private String content;

    private String cardColor;

    private List<Long> participantsId;

    public ScheduleServiceForm(Long id, String username, ScheduleForm scheduleForm) {
        this.id = id;
        this.username = username;
        this.date = scheduleForm.getDate();
        this.time = scheduleForm.getTime();
        this.subject = scheduleForm.getSubject();
        this.content = scheduleForm.getContent();
        this.cardColor = scheduleForm.getCardColor();
        this.participantsId = scheduleForm.getParticipantsId();
    }
}
