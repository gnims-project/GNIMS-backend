package com.gnims.project.share.persistence.embedded;

import com.gnims.project.domain.schedule.dto.ScheduleCreatedEvent;
import com.gnims.project.domain.schedule.dto.UpdateForm;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Embeddable
@NoArgsConstructor
public class Appointment {

    private LocalDate date;
    private LocalTime time;

    public Appointment(UpdateForm form) {
        this.date = form.getDate();
        this.time = form.getTime();
    }

    public Appointment(ScheduleCreatedEvent form) {
        this.date = form.getDate();
        this.time = form.getTime();
    }
}
