package com.gnims.project.domain.schedule.controller;

import com.gnims.project.domain.schedule.dto.*;
import com.gnims.project.domain.schedule.service.ScheduleService;
import com.gnims.project.security.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    //스케줄 등록
    @PostMapping("/events")
    public ResponseEntity<SimpleScheduleResult> createSchedule(@RequestBody ScheduleForm scheduleForm) {
        scheduleService.makeSchedule(scheduleForm);
        return new ResponseEntity<>(new SimpleScheduleResult(201, "스케줄 생성 완료"), HttpStatus.CREATED);
    }

    //스케줄 전체 조회
    @GetMapping("/events")
    public ResponseEntity<ReadScheduleResult> readAllSchedule(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.receiveUserId();
        List<ReadPastAllResponse> responses = scheduleService.readAllSchedule(userId);
        return new ResponseEntity<>(new ReadScheduleResult<>(200, "전체 조회 완료", responses), HttpStatus.OK);
    }

    //스케줄 단건 조회
    @GetMapping("/events/{event-id}")
    public ResponseEntity<ReadScheduleResult> readOneSchedule(@PathVariable("event-id") Long eventId) {
        ReadOneResponse response = scheduleService.readOneSchedule(eventId);
        return new ResponseEntity<>(new ReadScheduleResult(200, "상세 조회 완료", response), HttpStatus.OK);
    }

    // 수락을 대기중인 스케줄
    @GetMapping("/events/pending")
    public ResponseEntity<ReadScheduleResult> readPendingSchedule(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.receiveUserId();
        List<ReadPastAllResponse> responses = scheduleService.readPendingSchedule(userId);
        return new ResponseEntity<>(new ReadScheduleResult<>(200, "수락 대기중인 스케줄 조회 완료", responses), HttpStatus.OK);
    }

    // 과거 일정 조회
    @GetMapping("/events/past")
    public ResponseEntity<ReadScheduleResult> readPastSchedule(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.receiveUserId();
        List<ReadPastAllResponse> responses = scheduleService.readPastSchedule(userId);
        return new ResponseEntity<>(new ReadScheduleResult<>(200, "과거 스케줄 조회 완료", responses), HttpStatus.OK);
    }


    //스케줄 수락하기
    @PostMapping("/events/{event-id}/acceptance")
    public ResponseEntity<SimpleScheduleResult> readPendingSchedule(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                    @PathVariable("event-id") Long eventId) {
        Long userId = userDetails.receiveUserId();
        scheduleService.acceptSchedule(userId, eventId);
        return new ResponseEntity<>(new SimpleScheduleResult(200, "스케줄을 수락합니다."), HttpStatus.OK);
    }

    //스케줄 삭제
    @DeleteMapping("/events/{event-id}")
    public ResponseEntity<SimpleScheduleResult> deleteSchedule(@PathVariable("event-id") Long eventId,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {

        scheduleService.softDeleteSchedule(userDetails.receiveUserId(), eventId);
        return new ResponseEntity<>(new SimpleScheduleResult(200, "스케줄을 삭제합니다."), HttpStatus.OK);
    }

    //스케줄 수정
    @PutMapping("/events/{event-id}")
    public ResponseEntity<SimpleScheduleResult> updateSchedule(@PathVariable("event-id") Long eventId,
                                                               @RequestBody UpdateForm updateForm,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {

        scheduleService.updateSchedule(userDetails.receiveUserId(), updateForm, eventId);
        return new ResponseEntity<>(new SimpleScheduleResult(200, "스케줄을 수정합니다."), HttpStatus.OK);
    }
}