package com.gnims.project.domain.schedule.controller;

import com.gnims.project.domain.schedule.dto.*;
import com.gnims.project.domain.schedule.service.ScheduleService;
import com.gnims.project.security.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.gnims.project.util.ResponseMessage.*;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@Transactional
public class ScheduleController {

    private final ScheduleService scheduleService;

    //스케줄 전체 조회 기본 버전 - user-id 붙인 이유는 타인의 일정도 볼 수 있어야 하기 때문에
    @GetMapping("/proto/users/{user-id}/events")
    public ResponseEntity<ReadScheduleResult> readAllScheduleProto(@PathVariable("user-id") Long followId) {
        List<ReadAllResponse> responses = scheduleService.readAllScheduleProto(followId);
        return new ResponseEntity<>(new ReadScheduleResult<>(200, READ_ALL_SCHEDULE_MESSAGE, responses), OK);
    }

    //스케줄 전체 조회  DTO **한방쿼리**
    @GetMapping("/users/{user-id}/events")
    public ResponseEntity<ReadScheduleResult> readAllSchedule(@PathVariable("user-id") Long followId) {
        List<ReadAllResponse> responses = scheduleService.readAllSchedule(followId);
        return new ResponseEntity<>(new ReadScheduleResult<>(200, READ_ALL_SCHEDULE_MESSAGE, responses), OK);
    }

    //스케줄 전체 조회 최적화 진행중 - Paging, dto 버전 **한방쿼리**
    @GetMapping("/v2-page/users/{user-id}/events")
    public ResponseEntity<PageableReadScheduleResult> readAllSchedulePageV3(@PathVariable("user-id") Long followId,
                                                                            @RequestParam Integer page,
                                                                            @RequestParam Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        PageableReadResponse response = scheduleService.readAllSchedulePage(followId, pageRequest);
        return new ResponseEntity<>(new PageableReadScheduleResult<>(200, READ_ALL_SCHEDULE_MESSAGE,
                response.getSize(), response.getData()), OK);
    }

    //스케줄 단건 조회 - 기본 버전
    @GetMapping("/proto/events/{event-id}")
    public ResponseEntity<ReadScheduleResult> readOneScheduleProto(@PathVariable("event-id") Long eventId) {
        ReadOneResponse response = scheduleService.readOneScheduleProto(eventId);
        return new ResponseEntity<>(new ReadScheduleResult(200, READ_ONE_SCHEDULE_MESSAGE, response), OK);
    }

    //스케줄 단건 조회 - 쿼리 최적화 DTO **한방쿼리**
    @GetMapping("/events/{event-id}")
    public ResponseEntity<ReadScheduleResult> readOneSchedule(@PathVariable("event-id") Long eventId) {
        ReadOneResponse response = scheduleService.readOneSchedule(eventId);
        return new ResponseEntity<>(new ReadScheduleResult(200, READ_ONE_SCHEDULE_MESSAGE, response), OK);
    }

    // 수락 대기중인 스케줄 조회
    @GetMapping("/events/pending")
    public ResponseEntity<ReadScheduleResult> readPendingSchedule(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.receiveUserId();
        List<ReadPastAllResponse> responses = scheduleService.readPendingSchedule(userId);
        return new ResponseEntity<>(new ReadScheduleResult<>(200, READ_PENDING_SCHEDULE_MESSAGE, responses), OK);
    }

    @GetMapping("/v2/events/pending")
    public ResponseEntity<ReadScheduleResult> readPendingScheduleV2(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.receiveUserId();
        List<ReadPendingDto> responses = scheduleService.readPendingScheduleV2(userId);
        return new ResponseEntity<>(new ReadScheduleResult<>(200, READ_PENDING_SCHEDULE_MESSAGE, responses), OK);
    }

    // 과거 스케줄 조회
    @GetMapping("/events/past")
    public ResponseEntity<ReadScheduleResult> readPastScheduleV2(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.receiveUserId();
        List<ReadAllResponse> responses = scheduleService.readPastSchedule(userId);
        return new ResponseEntity<>(new ReadScheduleResult<>(200, READ_PAST_SCHEDULE_MESSAGE, responses), OK);
    }

    //스케줄 수락
    @PostMapping("/events/{event-id}/acceptance")
    public ResponseEntity<SimpleScheduleResult> acceptSchedule(@PathVariable("event-id") Long eventId,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {

        scheduleService.acceptSchedule(userDetails.receiveUserId(), eventId);
        return new ResponseEntity<>(new SimpleScheduleResult(200, ACCEPT_SCHEDULE_MESSAGE), OK);
    }

    //스케줄 거절
    @PostMapping("/events/{event-id}/rejection")
    public ResponseEntity<SimpleScheduleResult> rejectSchedule(@PathVariable("event-id") Long eventId,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {

        scheduleService.rejectSchedule(userDetails.receiveUserId(), eventId);
        return new ResponseEntity<>(new SimpleScheduleResult(200, REJECT_SCHEDULE_MESSAGE), OK);
    }


    //스케줄 삭제
    @DeleteMapping("/events/{event-id}")
    public ResponseEntity<SimpleScheduleResult> deleteSchedule(@PathVariable("event-id") Long eventId,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {

        scheduleService.softDeleteSchedule(userDetails.receiveUserId(), eventId);
        return new ResponseEntity<>(new SimpleScheduleResult(200, DELETE_SCHEDULE_MESSAGE), OK);
    }

    //스케줄 수정
    @PutMapping("/events/{event-id}")
    public ResponseEntity<SimpleScheduleResult> updateSchedule(@PathVariable("event-id") Long eventId,
                                                               @RequestBody @Valid UpdateForm updateForm,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {

        scheduleService.updateSchedule(userDetails.receiveUserId(), updateForm, eventId);
        return new ResponseEntity<>(new SimpleScheduleResult(200, UPDATE_SCHEDULE_MESSAGE), OK);
    }
}