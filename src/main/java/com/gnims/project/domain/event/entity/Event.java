package com.gnims.project.domain.event.entity;

import com.gnims.project.domain.schedule.dto.ScheduleCreatedEvent;
import com.gnims.project.domain.schedule.dto.UpdateForm;
import com.gnims.project.domain.schedule.entity.Schedule;
import com.gnims.project.share.persistence.embedded.Appointment;
import com.gnims.project.share.persistence.superclass.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static com.gnims.project.share.message.ExceptionMessage.ALREADY_DELETED_EVENT;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event extends BaseEntity {

    @Id
    @Column(name = "event_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Appointment appointment;

    private String cardColor;

    private String subject;

    @Lob
    private String content;

    private Boolean isDeleted;

    @OneToMany(mappedBy = "event")
    private List<Schedule> schedule = new ArrayList<>();

    private Long dDay;

    public Event(Appointment appointment, ScheduleCreatedEvent form) {
        this.appointment = appointment;
        this.subject = form.getSubject();
        this.content = form.getContent();
        this.cardColor = form.getCardColor();
        this.isDeleted = false;
        this.dDay = calculateDDay();
    }

    public void removeEvent() {
        isAlreadyRemoved();
        this.isDeleted = true;
    }

    public void updateEvent(UpdateForm form) {
        isAlreadyRemoved();
        this.cardColor = form.getCardColor();
        this.subject = form.getSubject();
        this.content = form.getContent();
        this.appointment = new Appointment(form);
        this.dDay = calculateDDay();
    }

    private void isAlreadyRemoved() {
        if (isDeleted) {
            throw new IllegalArgumentException(ALREADY_DELETED_EVENT);
        }
    }

    private Long calculateDDay() {
        return ChronoUnit.DAYS.between(LocalDate.now(), appointment.getDate());
    }

    public Boolean isNotDeleted() {
        return !this.isDeleted;
    }

    public Boolean isNotPast() {
        return this.dDay >= 0L;
    }
}
