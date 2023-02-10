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
        SimpleScheduleResult result = scheduleService.makeSchedule(scheduleForm);
        return new ResponseEntity<>(result, HttpStatus.valueOf(result.getStatus()));
    }

    //스케줄 전체 조회
    @GetMapping("/users/{user-id}/events")
    public ResponseEntity<ReadScheduleResult> readAllSchedule(@PathVariable("user-id") Long userId) {
        List<ReadAllResponse> responses = scheduleService.readAllSchedule(userId);
        return new ResponseEntity<>(new ReadScheduleResult<>(200, "전체 조회 완료", responses), HttpStatus.OK);
    }

    //스케줄 단건 조회
    @GetMapping("/users/events/{event-id}")
    public ResponseEntity<ReadScheduleResult> readOneSchedule(@PathVariable("event-id") Long eventId) {
        ReadOneResponse readOneResponse = scheduleService.readOneSchedule(eventId);
        return new ResponseEntity<>(new ReadScheduleResult(200, "상세 조회 완료", readOneResponse), HttpStatus.OK);
    }

    // 수락을 대기중인 스케줄
    @GetMapping("/users/{user-id}/events/pending")
    public ResponseEntity<ReadScheduleResult> readPendingSchedule(@PathVariable("user-id") Long userId) {
        List<ReadAllResponse> responses = scheduleService.readPendingSchedule(userId);
        return new ResponseEntity<>(new ReadScheduleResult<>(200, "수락 대기중인 스케줄 조회 완료", responses), HttpStatus.OK);
    }

    //스케줄 수락하기
    @PostMapping("/users/{user-id}/events/{event-id}/acceptance")
    public ResponseEntity<SimpleScheduleResult> readPendingSchedule(@PathVariable("user-id") Long userId,
                                                                    @PathVariable("event-id") Long eventId) {
        scheduleService.acceptSchedule(userId, eventId);
        return new ResponseEntity<>(new SimpleScheduleResult(200, "스케줄을 수락합니다."), HttpStatus.OK);
    }

    //스케줄 삭제
    @DeleteMapping("/users/events/{event-id}")
    public ResponseEntity<SimpleScheduleResult> deleteSchedule(@PathVariable("event-id") Long eventId,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {

        scheduleService.softDeleteSchedule(userDetails.getUser().getId(), eventId);
        return new ResponseEntity<>(new SimpleScheduleResult(200, "스케줄을 삭제합니다."), HttpStatus.OK);
    }
}