package com.gnims.project.domain.event.entity;

import com.gnims.project.domain.schedule.dto.ScheduleForm;
import com.gnims.project.domain.schedule.dto.ScheduleServiceForm;
import com.gnims.project.domain.schedule.dto.UpdateForm;
import com.gnims.project.domain.schedule.entity.Schedule;
import com.gnims.project.share.persistence.embedded.Appointment;
import com.gnims.project.share.persistence.superclass.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event extends BaseEntity {

    @Id @Column(name = "event_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Appointment appointment;

    private String cardColor;
    private String subject;
    private String content;
    private Boolean isDeleted;

    @OneToMany(mappedBy = "event")
    private List<Schedule> schedule = new ArrayList<>();

    private Long dDay;

    public Event(Appointment appointment, ScheduleForm form) {
        this.appointment = appointment;
        this.subject = form.getSubject();
        this.content = form.getContent();
        this.cardColor = form.getCardColor();
        this.isDeleted = false;
        this.dDay = calculateDDay();
    }

    public Event(Appointment appointment, ScheduleServiceForm form) {
        this.appointment = appointment;
        this.subject = form.getSubject();
        this.content = form.getContent();
        this.cardColor = form.getCardColor();
        this.isDeleted = false;
        this.dDay = calculateDDay();
    }

    public void removeEvent() {
        this.isDeleted = true;
    }

    public void updateEvent(UpdateForm form) {
        this.cardColor = form.getCardColor();
        this.subject = form.getSubject();
        this.content = form.getContent();
        this.appointment = new Appointment(form);
        this.dDay = calculateDDay();
    }

    public LocalDate receiveDate() {
        return this.appointment.getDate();
    }

    public LocalTime receiveTime() {
        return this.appointment.getTime();
    }

    private long calculateDDay() {
        return ChronoUnit.DAYS.between(LocalDate.now(), appointment.getDate());
    }

}
