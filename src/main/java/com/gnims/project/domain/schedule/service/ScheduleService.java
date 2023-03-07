package com.gnims.project.domain.schedule.service;

import com.gnims.project.domain.event.entity.Event;
import com.gnims.project.domain.event.repository.EventRepository;
import com.gnims.project.domain.friendship.entity.Friendship;
import com.gnims.project.domain.friendship.repository.FriendshipRepository;
import com.gnims.project.domain.friendship.service.FriendshipService;
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
import java.util.Optional;
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
    private final FriendshipService friendshipService;
    private final FriendshipRepository friendshipRepository;

    public void makeSchedule(ScheduleServiceForm form) {
        //이벤트 엔티티 생성 및 저장
        Long userId = form.getCreateBy();
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

    public List<ReadAllResponse> readAllSchedule(Long myselfId, Long userId) {
        //만약 userId userDetails 가 일치하지 않는다. -> 팔로우 일정을 조회하겠다는 의미
        validateAccessibility(myselfId, userId);

        List<ReadAllScheduleDto> eventQueries = scheduleRepository.readAllSchedule(userId);

        //필요한 로직인지 확인 필요
        HashSet<Long> set = new HashSet<>(eventQueries.size());

        List<ReadAllScheduleDto> event = eventQueries.stream()
                .filter(e -> set.add(e.getEventId()))
                .collect(toList());
        //필요한 로직인지 확인 필요
        return event.stream().map(e -> new ReadAllResponse(
                e.getEventId(),
                e.getDate(),
                e.getTime(),
                e.getCardColor(),
                e.getSubject(),
                e.getDDay(),
                receiveInvitees(eventQueries.stream(), e))).collect(toList());

    }

    private static List<String> sorts = List.of("event.createAt", "event.dDay");

    public PageableReadResponse readAllSchedulePage(Long userId, PageRequest pageRequest, String sorting) {
        if (!sorts.contains(sorting)) {
            throw new IllegalArgumentException(BAD_REQUEST);
        }
        // 이베트 생성순일때는 내림차순으로 변경
        if ("event.createAt".equals(sorting)) {
            pageRequest = pageRequest.withSort(pageRequest.getSort().descending());
        }

        Page<ReadAllScheduleDto> schedules = scheduleRepository.readAllSchedulePage(userId, pageRequest);

        List<ReadAllResponse> responses = schedules.stream().map(ds -> new ReadAllResponse(
                ds.getEventId(),
                ds.getDate(),
                ds.getTime(),
                ds.getCardColor(),
                ds.getSubject(),
                ds.getDDay(),
                receiveInvitees(schedules.stream(), ds))).collect(toList());

        return new PageableReadResponse(schedules.getTotalPages(), responses);
    }

    public ReadOneResponse readOneSchedule(Long myselfId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException(NOT_EXISTED_SCHEDULE));

        validateAccessibility(myselfId, event);

        List<ReadOneScheduleDto> events = scheduleRepository.readOneSchedule(eventId);

        ReadOneScheduleDto eventDto = events.get(0);
        return new ReadOneResponse(
                eventDto.getEventId(),
                eventDto.getDate(),
                eventDto.getTime(),
                eventDto.getCardColor(),
                eventDto.getSubject(),
                eventDto.getContent(),
                eventDto.getHostId(),
                eventDto.getDDay(),
                events.stream().map(e -> new ReadOneUserDto(e.getUsername())).collect(toList())
        );
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

    private boolean isPersonalSchedule(ScheduleServiceForm form, Long userId) {
        return form.getParticipantsId().isEmpty() ||
                (form.getParticipantsId().size() == 1 && form.getParticipantsId().get(0).equals(userId));
    }

    private List<ReadAllUserDto> receiveInvitees(Stream<ReadAllScheduleDto> eventAllQueries, ReadAllScheduleDto ds) {
        return eventAllQueries
                .filter(eq -> eq.isSameEventId(ds.getEventId()))
                .map(eq -> new ReadAllUserDto(eq.getUsername(), eq.getProfile()))
                .collect(toList());
    }

    private void validateAccessibility(Long myId, Long userId) {
        if (!userId.equals(myId)) {
            List<Friendship> friendships = friendshipRepository.readAllFollowingOf(myId);
            List<Friendship> filterFriendships = friendships.stream()
                    .filter(f -> f.getFollow().getId().equals(userId)).collect(toList());

            if (filterFriendships.isEmpty()) {
                throw new SecurityException(FORBIDDEN);
            }
        }
    }

    private void validateAccessibility(Long myselfId, Event event) {
        // 내가 만든 일정인지 체크
        if (!event.getCreateBy().equals(myselfId)) {

            //팔로우 목록
            List<Friendship> friendships = friendshipRepository.readAllFollowingOf(myselfId);
            //팔로우 ID 불러오기
            List<Long> follows = friendships.stream().map(f -> f.getFollow().getId()).collect(toList());

            //팔로우가 포함된 일정인지 체크
            List<Schedule> schedules = scheduleRepository.findAllByEvent(event);
            List<Long> collect = schedules.stream()
                    .filter(s -> s.isAccepted() == true)
                    .filter(s -> s.getEvent().getIsDeleted() == false)
                    .filter(s -> s.getEvent().getDDay() >= 0)
                    .map(s -> s.getUser().getId()).collect(toList());
            Optional<Long> verification = follows.stream().filter(f -> collect.contains(f)).findFirst();


            if (verification.isEmpty()) {
                throw new SecurityException(FORBIDDEN);
            }
        }
    }
}
