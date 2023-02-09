package com.gnims.project.domain.event.entity;

import com.gnims.project.domain.schedule.dto.ScheduleForm;
import com.gnims.project.util.embedded.Appointment;
import com.gnims.project.util.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor
public class Event extends BaseEntity {

    @Id @Column(name = "event_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Appointment appointment;

    private String subject;
    private String content;


    public Event(Appointment appointment, ScheduleForm form) {
        this.appointment = appointment;
        this.subject = form.getSubject();
        this.content = form.getContent();
    }
}
