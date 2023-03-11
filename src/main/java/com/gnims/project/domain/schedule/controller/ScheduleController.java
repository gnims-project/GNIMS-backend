package com.gnims.project.domain.schedule.controller;

import com.gnims.project.domain.schedule.dto.*;
import com.gnims.project.domain.schedule.service.ScheduleService;
import com.gnims.project.security.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.gnims.project.share.message.ResponseMessage.*;
import static org.springframework.http.ResponseEntity.*;

@RestController
@RequiredArgsConstructor
@Transactional
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final ApplicationEventPublisher applicationEventPublisher;
    // 스케줄 등록 알람 버전
    @PostMapping("/events")
    public ResponseEntity<SimpleScheduleResult> createSchedule(@RequestBody @Valid ScheduleForm scheduleForm,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.receiveUserId();
        String username = userDetails.getUser().getUsername();
        ScheduleServiceForm serviceForm = scheduleForm.to(userId, username);
        scheduleService.makeSchedule(serviceForm);

        applicationEventPublisher.publishEvent(serviceForm);
        return new ResponseEntity<>(new SimpleScheduleResult(201, CREATE_SCHEDULE_MESSAGE), HttpStatus.CREATED);
    }

    //스케줄 전체 조회
    @GetMapping("/users/{user-id}/events")
    public ResponseEntity<ReadScheduleResult> readAllSchedule(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                              @PathVariable("user-id") Long followId) {
        List<ReadAllResponse> responses = scheduleService.readAllSchedule(userDetails.receiveUserId(), followId);

        return ok(new ReadScheduleResult<>(200, READ_ALL_SCHEDULE_MESSAGE, responses));
    }

    //스케줄 전체 조회
    @GetMapping("/v2/users/{user-id}/events")
    public ResponseEntity<PageScheduleResult> readAllSchedulePage(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                  @PathVariable("user-id") Long followId,
                                                                  @RequestParam(defaultValue = "0") Integer page,
                                                                  @RequestParam(defaultValue = "9999") Integer size,
                                                                  @RequestParam(defaultValue = "event.dDay") String sortedBy) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortedBy).ascending());
        PageableReadResponse response = scheduleService.readAllSchedule(userDetails.receiveUserId(),followId, pageRequest);
        return ok(new PageScheduleResult<>(200, READ_ALL_SCHEDULE_MESSAGE, response.getSize(), response.getData()));
    }

    //스케줄 단건 조회
    @GetMapping("/events/{event-id}")
    public ResponseEntity<ReadScheduleResult> readOneSchedule(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                              @PathVariable("event-id") Long eventId) {
        ReadOneResponse response = scheduleService.readOneSchedule(userDetails.receiveUserId(), eventId);
        return ok(new ReadScheduleResult(200, READ_ONE_SCHEDULE_MESSAGE, response));
    }

    // 수락 대기중인 스케줄 조회
    @GetMapping("/v2/events/pending")
    public ResponseEntity<ReadScheduleResult> readPendingSchedule(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.receiveUserId();
        List<ReadPendingResponse> responses = scheduleService.readPendingSchedule(userId);
        return ok(new ReadScheduleResult<>(200, READ_PENDING_SCHEDULE_MESSAGE, responses));
    }

    // 과거 스케줄 조회
    @GetMapping("/events/past")
    public ResponseEntity<ReadScheduleResult> readPastSchedule(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.receiveUserId();
        List<ReadAllResponse> responses = scheduleService.readPastSchedule(userId);
        return ok(new ReadScheduleResult<>(200, READ_PAST_SCHEDULE_MESSAGE, responses));
    }

    //스케줄 수락
    @PostMapping("/events/{event-id}/acceptance")
    public ResponseEntity<SimpleScheduleResult> acceptSchedule(@PathVariable("event-id") Long eventId,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ScheduleDecisionEventForm eventForm = scheduleService.acceptSchedule(userDetails.receiveUserId(), eventId);
        applicationEventPublisher.publishEvent(eventForm);
        return ok(new SimpleScheduleResult(200, ACCEPT_SCHEDULE_MESSAGE));
    }

    //스케줄 거절
    @PostMapping("/events/{event-id}/rejection")
    public ResponseEntity<SimpleScheduleResult> rejectSchedule(@PathVariable("event-id") Long eventId,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ScheduleDecisionEventForm EventForm = scheduleService.rejectSchedule(userDetails.receiveUserId(), eventId);
        applicationEventPublisher.publishEvent(EventForm);
        return ok(new SimpleScheduleResult(200, REJECT_SCHEDULE_MESSAGE));
    }
}