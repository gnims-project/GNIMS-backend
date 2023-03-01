package com.gnims.project.domain.schedule.entity;

import com.gnims.project.domain.event.entity.Event;
import com.gnims.project.domain.schedule.dto.ReadAllUserDto;
import com.gnims.project.domain.user.entity.User;

import com.gnims.project.share.persistence.superclass.TimeStamped;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.gnims.project.domain.schedule.entity.ScheduleStatus.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule extends TimeStamped {

    @Id @Column(name = "schedule_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="event_id")
    private Event event;

    @Enumerated(value = EnumType.STRING)
    private ScheduleStatus scheduleStatus;

    public Schedule(User user, Event event) {
        this.user = user;
        this.event = event;
        this.scheduleStatus = PENDING;
    }
    public void decideScheduleStatus(ScheduleStatus scheduleStatus) {
        this.scheduleStatus = scheduleStatus;
    }

    public Long receiveUserId() {
        return this.getUser().getId();
    }

    public boolean isAccepted() {
        return getScheduleStatus().equals(ACCEPT);
    }
}
