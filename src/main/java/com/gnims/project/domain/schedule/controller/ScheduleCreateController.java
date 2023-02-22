package com.gnims.project.domain.schedule.controller;

import com.gnims.project.domain.schedule.dto.ScheduleForm;
import com.gnims.project.domain.schedule.dto.ScheduleServiceForm;
import com.gnims.project.domain.schedule.dto.SimpleScheduleResult;
import com.gnims.project.domain.schedule.service.ScheduleService;
import com.gnims.project.security.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.gnims.project.share.message.ResponseMessage.CREATE_SCHEDULE_MESSAGE;

@RestController
@RequiredArgsConstructor
public class ScheduleCreateController {

    private final ScheduleService scheduleService;
    private final ApplicationEventPublisher applicationEventPublisher;

    //스케줄 등록
    @PostMapping("/events")
    public ResponseEntity<SimpleScheduleResult> createSchedule(@RequestBody @Valid ScheduleForm scheduleForm,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.receiveUserId();
        scheduleService.makeSchedule(scheduleForm, userId);

        applicationEventPublisher.publishEvent(scheduleForm);
        return new ResponseEntity<>(new SimpleScheduleResult(201, CREATE_SCHEDULE_MESSAGE), HttpStatus.CREATED);
    }
    // 알람 버전
    @PostMapping("/v2/events")
    public ResponseEntity<SimpleScheduleResult> createScheduleV2(@RequestBody @Valid ScheduleForm scheduleForm,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.receiveUserId();
        String username = userDetails.getUser().getUsername();
        ScheduleServiceForm serviceForm = scheduleForm.convertServiceForm(userId, username);
        scheduleService.makeScheduleV2(serviceForm);

        applicationEventPublisher.publishEvent(serviceForm);
        return new ResponseEntity<>(new SimpleScheduleResult(201, CREATE_SCHEDULE_MESSAGE), HttpStatus.CREATED);
    }
}
