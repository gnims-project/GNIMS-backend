package com.gnims.project.domain.schedule.service;

import com.gnims.project.domain.event.entity.Event;
import com.gnims.project.domain.event.repository.EventRepository;
import com.gnims.project.domain.schedule.dto.*;
import com.gnims.project.domain.schedule.entity.Schedule;
import com.gnims.project.domain.schedule.repository.ScheduleRepository;
import com.gnims.project.domain.user.entity.User;
import com.gnims.project.domain.user.repository.UserRepository;
import com.gnims.project.share.persistence.embedded.Appointment;
import io.jsonwebtoken.security.SecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static com.gnims.project.domain.schedule.entity.ScheduleStatus.*;
import static com.gnims.project.share.message.ExceptionMessage.*;
import static java.util.stream.Collectors.*;

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
                () -> new IllegalArgumentException(NOT_EXISTED_USER));
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
                .map(u -> new Schedule(u, event)).collect(toList());
        schedules.add(hostSchedule);
        scheduleRepository.saveAll(schedules);
    }

    public void makeScheduleV2(ScheduleServiceForm form) {
        //이벤트 엔티티 생성 및 저장
        Long userId = form.getId();
        Event event = eventRepository.save(new Event(new Appointment(form), form));
        //주최자 스케줄 처리 -> 주최자는 자동 일정에 자동 참여
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException(NOT_EXISTED_USER));
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
                .map(u -> new Schedule(u, event)).collect(toList());
        schedules.add(hostSchedule);
        scheduleRepository.saveAll(schedules);
    }

    @Deprecated
    public List<ReadAllResponse> readAllScheduleProto(Long userId) {
        List<Schedule> schedules = scheduleRepository.findAllByUser_IdAndScheduleStatusIsAndEvent_IsDeletedIs(userId, ACCEPT, false);

        return schedules.stream().map(s -> new ReadAllResponse(
                s.getEvent().getId(),
                s.getEvent().receiveDate(),
                s.getEvent().receiveTime(),
                s.getEvent().getCardColor(),
                s.getEvent().getSubject(),
                s.getEvent().getDDay(),
                s.findInvitees())).collect(toList());
    }

    public List<ReadAllResponse> readAllSchedule(Long userId) {
        List<ReadAllScheduleDto> eventQueries = scheduleRepository.readAllSchedule(userId);
        HashSet<Long> set = new HashSet<>(eventQueries.size());

        List<ReadAllScheduleDto> event = eventQueries.stream()
                .filter(e -> set.add(e.getEventId()))
                .collect(toList());

        return event.stream().map(e -> new ReadAllResponse(
                e.getEventId(),
                e.getDate(),
                e.getTime(),
                e.getCardColor(),
                e.getSubject(),
                e.getDDay(),
                receiveInvitees(eventQueries.stream(), e))).collect(toList());

    }

    public PageableReadResponse readAllSchedulePage(Long userId, PageRequest pageRequest) {
        Page<ReadAllScheduleDto> schedules = scheduleRepository.readAllSchedulePage(userId, pageRequest);
        Long totalElements = schedules.getTotalElements();
        int eventSize = totalElements.intValue();

        // 이벤트는 중복될 수 있음으로 set으로 중복 처리
        HashSet<Long> set = new HashSet<>(eventSize);
        List<ReadAllScheduleDto> distinctSchedules = schedules.stream().filter(e -> set.add(e.getEventId())).collect(toList());

        List<ReadAllResponse> responses = distinctSchedules.stream().map(ds -> new ReadAllResponse(
                ds.getEventId(),
                ds.getDate(),
                ds.getTime(),
                ds.getCardColor(),
                ds.getSubject(),
                ds.getDDay(),
                receiveInvitees(schedules.stream(), ds))).collect(toList());

        return new PageableReadResponse(schedules.getTotalPages(), responses);
    }

    private static List<ReadAllUserDto> receiveInvitees(Stream<ReadAllScheduleDto> eventAllQueries, ReadAllScheduleDto ds) {
        return eventAllQueries
                .filter(eq -> eq.isSameEventId(ds.getEventId()))
                .map(eq -> new ReadAllUserDto(eq.getUsername(), eq.getProfile()))
                .collect(toList());
    }


    @Deprecated
    public ReadOneResponse readOneScheduleProto(Long eventId) {
        List<Schedule> schedules = scheduleRepository.findByEvent_IdAndScheduleStatusIs(eventId, ACCEPT);

        Event event = schedules.get(0).getEvent();

        checkIsDeleted(event);

        List<ReadOneUserDto> invitees = schedules.stream()
                .filter(s -> s.isAccepted())
                .map(s -> new ReadOneUserDto(s.getUser().getUsername()))
                .collect(toList());

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
        List<ReadOneScheduleDto> events = scheduleRepository.readOneSchedule(eventId);
        if (events.isEmpty()) {
            throw new IllegalArgumentException(NOT_EXISTED_SCHEDULE);
        }

        ReadOneScheduleDto event = events.get(0);
        return new ReadOneResponse(
                event.getEventId(),
                event.getDate(),
                event.getTime(),
                event.getCardColor(),
                event.getSubject(),
                event.getContent(),
                event.getHostId(),
                event.getDDay(),
                events.stream().map(e -> new ReadOneUserDto(e.getUsername())).collect(toList())
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
                s.findInvitees())).collect(toList());
    }

    public List<ReadPendingResponse> readPendingScheduleV2(Long userId) {
        List<ReadPendingDto> schedules = scheduleRepository.readAllPendingSchedule(userId);

        return schedules.stream().map(s -> new ReadPendingResponse(
                s.getEvent_Id(),
                s.getUsername(),
                s.getDate(),
                s.getTime(),
                s.getCard_Color(),
                s.getSubject())).collect(toList());
    }

    public List<ReadAllResponse> readPastSchedule(Long userId) {
        List<ReadAllScheduleDto> schedules = scheduleRepository.readPastSchedule(userId);

        HashSet<Long> set = new HashSet<>(schedules.size());
        List<ReadAllScheduleDto> distinctSchedule = schedules.stream()
                .filter(e -> set.add(e.getEventId())).collect(toList());

        return distinctSchedule.stream().map(ds -> new ReadAllResponse(
                ds.getEventId(),
                ds.getDate(),
                ds.getTime(),
                ds.getCardColor(),
                ds.getSubject(),
                ds.getDDay(),
                receiveInvitees(schedules.stream(), ds))).collect(toList());
    }

    @Transactional
    public void acceptSchedule(Long userId, Long eventId) {
        Schedule schedule = scheduleRepository.readOnePendingSchedule(userId, eventId)
                .orElseThrow(() -> new IllegalArgumentException(ALREADY_PROCESSED_OR_NOT_EXISTED_SCHEDULE));

        schedule.decideScheduleStatus(ACCEPT);
        scheduleRepository.save(schedule);
    }

    @Transactional
    public void rejectSchedule(Long userId, Long eventId) {
        Schedule schedule = scheduleRepository.readOnePendingSchedule(userId, eventId)
                .orElseThrow(() -> new IllegalArgumentException(ALREADY_PROCESSED_OR_NOT_EXISTED_SCHEDULE));

        schedule.decideScheduleStatus(REJECT);
        scheduleRepository.save(schedule);
    }

    @Transactional
    public void softDeleteSchedule(Long userId, Long eventId) {
        Event event = eventRepository.findByCreateByAndId(userId, eventId)
                .orElseThrow(() -> new SecurityException(ALREADY_PROCESSED_OR_NO_AUTHORITY_SCHEDULE));

        checkIsDeleted(event);

        event.removeEvent();
        eventRepository.save(event);
    }

    @Transactional
    public void updateSchedule(Long userId, UpdateForm updateForm, Long eventId) {
        Event event = eventRepository.findByCreateByAndId(userId, eventId).orElseThrow(
                () -> new SecurityException(ALREADY_PROCESSED_OR_NO_AUTHORITY_SCHEDULE));

        checkIsDeleted(event);

        event.updateEvent(updateForm);
        eventRepository.save(event);
    }

    private static void checkIsDeleted(Event event) {
        if (event.getIsDeleted().equals(true)) {
            throw new IllegalArgumentException(ALREADY_DELETED_EVENT);
        }
    }

    private static List<Schedule> filterDeletedSchedules(List<Schedule> schedules) {
        List<Schedule> liveSchedules = schedules.stream()
                .filter(s -> s.isDeletedEvent())
                .collect(toList());
        return liveSchedules;
    }

    private static boolean isPersonalSchedule(ScheduleForm form, Long userId) {
        return form.getParticipantsId().isEmpty() ||
                (form.getParticipantsId().size() == 1 && form.getParticipantsId().get(0).equals(userId));
    }

    private static boolean isPersonalSchedule(ScheduleServiceForm form, Long userId) {
        return form.getParticipantsId().isEmpty() ||
                (form.getParticipantsId().size() == 1 && form.getParticipantsId().get(0).equals(userId));
    }
}
