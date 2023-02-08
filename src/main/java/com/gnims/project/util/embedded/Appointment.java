package com.gnims.project.util.embedded;

import com.gnims.project.domain.schedule.dto.ScheduleForm;
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

    public Appointment(ScheduleForm form) {
        this.date = form.getDate();
        this.time = form.getTime();
    }
}
