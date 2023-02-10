package com.gnims.project.domain.event.entity;

import com.gnims.project.domain.schedule.dto.ScheduleForm;
import com.gnims.project.domain.schedule.entity.Schedule;
import com.gnims.project.util.embedded.Appointment;
import com.gnims.project.util.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
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

    public Event(Appointment appointment, ScheduleForm form) {
        this.appointment = appointment;
        this.subject = form.getSubject();
        this.content = form.getContent();
        this.cardColor = form.getCardColor();
        this.isDeleted = false;
    }

    public void removeEvent() {
        this.isDeleted = true;
    }
}
