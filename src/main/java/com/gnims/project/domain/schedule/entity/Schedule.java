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

    private Boolean isAttend;

    @Enumerated(value = EnumType.STRING)
    private ScheduleStatus scheduleStatus;

    public Schedule(User user, Event event) {
        this.user = user;
        this.event = event;
        this.isAttend = false;
        this.scheduleStatus = PENDING;
    }

    public String receiveUsername() {
        return this.getUser().getUsername();
    }
    public String receiveProfile() {
        return user.getProfileImage();
    }
    // 1대 다 조회
    public List<ReadAllUserDto> findInvitees() {
        return event.getSchedule().stream()
                .filter(schedule -> schedule.isAccepted())
                .map(s -> new ReadAllUserDto(s.receiveUsername(), s.receiveProfile()))
                .collect(Collectors.toList());
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

    public boolean isDeletedEvent() {
        return event.getIsDeleted().equals(false);
    }
}
