package com.gnims.project.domain.schedule.dto;

import com.gnims.project.domain.schedule.entity.ScheduleStatus;
import lombok.Getter;

@Getter
public class ScheduleDecisionEventForm {
    private final Long senderId;
    private final String subject;
    private final String senderName;
    private final Long receiverId;
    private final ScheduleStatus scheduleStatus;

    public ScheduleDecisionEventForm(Long senderId, String subject, String senderName, Long receiverId, ScheduleStatus scheduleStatus) {
        this.senderId = senderId;
        this.subject = subject;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.scheduleStatus = scheduleStatus;
    }
}
