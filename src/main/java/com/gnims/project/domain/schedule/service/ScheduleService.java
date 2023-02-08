package com.gnims.project.domain.schedule.service;

import com.gnims.project.domain.event.entity.Event;
import com.gnims.project.domain.event.repository.EventRepository;
import com.gnims.project.domain.schedule.dto.*;
import com.gnims.project.domain.schedule.entity.Schedule;
import com.gnims.project.domain.schedule.repository.ScheduleRepository;
import com.gnims.project.domain.user.entity.User;
import com.gnims.project.domain.user.repository.UserRepository;
import com.gnims.project.util.embedded.Appointment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public SimpleScheduleResult makeSchedule(ScheduleForm form) {
        //이벤트 엔티티 생성 및 저장
        Appointment appointment = new Appointment(form);
        Event event = new Event(appointment, form);
        Event saveEvent = eventRepository.save(event);

        //초대된 사용자 목록
        List<User> users = userRepository.findAllById(form.getParticipantsId());

        //User 들이 팔로우 인지 확인할 필요성 있음 (개선 버전에서 추가)

        //스케쥴 엔티티 생성 및 저장
        List<Schedule> schedules = users.stream().map(user -> new Schedule(user, saveEvent))
                .collect(Collectors.toList());
        scheduleRepository.saveAll(schedules);

        return new SimpleScheduleResult(200, "일정 조회 완료");
    }
}
