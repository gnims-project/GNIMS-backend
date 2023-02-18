package com.gnims.project.domain.schedule.service;

import com.gnims.project.domain.event.entity.Event;
import com.gnims.project.domain.event.repository.EventRepository;
import com.gnims.project.domain.schedule.dto.*;
import com.gnims.project.domain.schedule.entity.Schedule;
import com.gnims.project.domain.schedule.repository.ScheduleRepository;
import com.gnims.project.domain.user.entity.User;
import com.gnims.project.domain.user.repository.UserRepository;
import com.gnims.project.util.embedded.Appointment;
import io.jsonwebtoken.security.SecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.gnims.project.domain.schedule.entity.ScheduleStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public void makeSchedule(ScheduleForm form, Long userId) {
        //이벤트 엔티티 생성 및 저장
        Event event = eventRepository.save(new Event(new Appointment(form), form));
        //주최자 스케줄 처리 -> 주최자는 자동 일정에 자동 참여
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        Schedule hostSchedule = new Schedule(user, event);
        hostSchedule.decideScheduleStatus(ACCEPT);
        //개인 스케줄일 경우
        if (isPersonalSchedule(form, userId)) {
            scheduleRepository.save(hostSchedule);
            return;
        }
        //초대된 사용자 목록
        List<User> users = userRepository.findAllById(form.getParticipantsId());
        //User 들이 팔로우 인지 확인할 필요성 있음 (개선 버전에서 추가)
        //스케쥴 엔티티 생성 및 저장
        List<Schedule> schedules = users.stream()
                //자기 자신이 participants 목록에 들어갈 경우 필터링
                .filter(u -> !userId.equals(u.getId()))
                .map(u -> new Schedule(u, event)).collect(Collectors.toList());
        schedules.add(hostSchedule);
        scheduleRepository.saveAll(schedules);
    }

    public List<ReadAllResponse> readAllScheduleProto(Long userId) {
        List<Schedule> schedules = scheduleRepository.findAllByUser_IdAndScheduleStatusIsAndEvent_IsDeletedIs(userId, ACCEPT, false);

        return schedules.stream().map(s -> new ReadAllResponse(
                s.getEvent().getId(),
                s.getEvent().receiveDate(),
                s.getEvent().receiveTime(),
                s.getEvent().getCardColor(),
                s.getEvent().getSubject(),
                s.getEvent().getDDay(),
                s.findInvitees()
        )).collect(Collectors.toList());
    }

    public List<ReadAllResponse> readAllSchedule(Long userId) {
        List<EventAllQueryDto> eventQueries = scheduleRepository.readAllSchedule(userId);
        HashSet<Long> set = new HashSet<>(eventQueries.size());

        List<EventAllQueryDto> event = eventQueries.stream()
                .filter(e -> set.add(e.getEventId()))
                .collect(Collectors.toList());

        return event.stream().map(e -> new ReadAllResponse(
                e.getEventId(),
                e.getDate(),
                e.getTime(),
                e.getCardColor(),
                e.getSubject(),
                e.getDDay(),
                eventQueries.stream().filter(eq -> eq.getEventId().equals(e.getEventId()))
                        .map(eq -> new ReadAllUserDto(eq.getUsername(),eq.getProfile()))
                        .collect(Collectors.toList())
        )).collect(Collectors.toList());

    }

    public List<ReadAllResponse> readAllSchedulePage(Long userId, PageRequest pageRequest) {
        List<EventAllQueryDto> eventAllQueries = scheduleRepository.readAllSchedulePage(userId, pageRequest);

        // 이벤트는 중복될 수 있음으로 set으로 중복 처리
        HashSet<Long> set = new HashSet<>(eventAllQueries.size());
        List<EventAllQueryDto> event = eventAllQueries.stream()
                .filter(e -> set.add(e.getEventId()))
                .collect(Collectors.toList());

        return event.stream().map(e -> new ReadAllResponse(
                e.getEventId(),
                e.getDate(),
                e.getTime(),
                e.getCardColor(),
                e.getSubject(),
                e.getDDay(),
                eventAllQueries.stream().filter(eq -> eq.getEventId().equals(e.getEventId()))
                        .map(eq -> new ReadAllUserDto(eq.getUsername(),eq.getProfile()))
                        .collect(Collectors.toList())
        )).collect(Collectors.toList());
    }

    public ReadOneResponse readOneScheduleProto(Long eventId) {
        List<Schedule> schedules = scheduleRepository.findByEvent_IdAndScheduleStatusIs(eventId, ACCEPT);

        Event event = schedules.get(0).getEvent();

        checkIsDeleted(event);

        List<ReadOneUserDto> invitees = schedules.stream()
                .filter(s -> s.isAccepted())
                .map(s -> new ReadOneUserDto(s.getUser().getUsername()))
                .collect(Collectors.toList());

        return new ReadOneResponse(
                event.getId(),
                event.receiveDate(),
                event.receiveTime(),
                event.getCardColor(),
                event.getSubject(),
                event.getContent(),
                event.getDDay(),
                invitees);
    }

    public ReadOneResponse readOneSchedule(Long eventId) {
        List<EventOneQueryDto> events = scheduleRepository.readOneSchedule(eventId);
        if (events.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 일정입니다.");
        }

        EventOneQueryDto event = events.get(0);
        return new ReadOneResponse(
                event.getEventId(),
                event.getDate(),
                event.getTime(),
                event.getCardColor(),
                event.getSubject(),
                event.getContent(),
                event.getDDay(),
                events.stream().map(e -> new ReadOneUserDto(e.getUsername())).collect(Collectors.toList())
        );
    }

    public List<ReadPastAllResponse> readPendingSchedule(Long userId) {
        List<Schedule> schedules = scheduleRepository.findAllByUser_IdAndScheduleStatusIs(userId, PENDING);

        List<Schedule> liveSchedules = filterDeletedSchedules(schedules);

        return liveSchedules.stream().map(s -> new ReadPastAllResponse(
                s.getEvent().getId(),
                s.getEvent().receiveDate(),
                s.getEvent().receiveTime(),
                s.getEvent().getCardColor(),
                s.getEvent().getSubject(),
                s.findInvitees())).collect(Collectors.toList());
    }

    public List<ReadPastAllResponse> readPastSchedule(Long userId) {
        List<Schedule> schedules = scheduleRepository.findAllByUser_IdAndScheduleStatusIs(userId, ACCEPT);
        List<Schedule> pastSchedules = schedules.stream().filter(s -> s.isDeletedEvent())
                .filter(s -> ChronoUnit.DAYS.between(LocalDate.now(), s.getEvent().receiveDate()) < 0)
                .collect(Collectors.toList());

        return pastSchedules.stream().map(s -> new ReadPastAllResponse(
                s.getEvent().getId(),
                s.getEvent().receiveDate(),
                s.getEvent().receiveTime(),
                s.getEvent().getCardColor(),
                s.getEvent().getSubject(),
                s.findInvitees())).collect(Collectors.toList());
    }

    @Transactional
    public void acceptSchedule(Long userId, Long eventId) {
        Schedule schedule = scheduleRepository.readOnePendingSchedule(userId, eventId)
                .orElseThrow(() -> new IllegalArgumentException("이미 요청이 처리되었거나 존재하지 않는 일정입니다."));

        schedule.decideScheduleStatus(ACCEPT);
        scheduleRepository.save(schedule);
    }

    @Transactional
    public void rejectSchedule(Long userId, Long eventId) {
        Schedule schedule = scheduleRepository.readOnePendingSchedule(userId, eventId)
                .orElseThrow(() -> new IllegalArgumentException("이미 요청이 처리되었거나 존재하지 않는 일정입니다."));

        schedule.decideScheduleStatus(REJECT);
        scheduleRepository.save(schedule);
    }

    @Transactional
    public void softDeleteSchedule(Long userId, Long eventId) {
        Event event = eventRepository.findByCreateByAndId(userId, eventId)
                .orElseThrow(() -> new SecurityException("이미 요청이 처리되었거나 삭제 권한이 없습니다."));

        checkIsDeleted(event);

        event.removeEvent();
        eventRepository.save(event);
    }

    @Transactional
    public void updateSchedule(Long userId, UpdateForm updateForm, Long eventId) {
        Event event = eventRepository.findByCreateByAndId(userId, eventId).orElseThrow(
                () -> new SecurityException("이미 요청이 처리되었거나 수정 권한이 없습니다."));

        checkIsDeleted(event);

        event.updateEvent(updateForm);
        eventRepository.save(event);
    }

    private static void checkIsDeleted(Event event) {
        if (event.getIsDeleted().equals(true)) {
            throw new IllegalArgumentException("이미 삭제된 일정입니다.");
        }
    }

    private static List<Schedule> filterDeletedSchedules(List<Schedule> schedules) {
        List<Schedule> liveSchedules = schedules.stream()
                .filter(s -> s.isDeletedEvent())
                .collect(Collectors.toList());
        return liveSchedules;
    }

    private static boolean isPersonalSchedule(ScheduleForm form, Long userId) {
        return form.getParticipantsId().isEmpty() ||
                (form.getParticipantsId().size() == 1 && form.getParticipantsId().get(0).equals(userId));
    }
}
