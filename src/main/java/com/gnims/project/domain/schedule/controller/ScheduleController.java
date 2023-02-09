package com.gnims.project.domain.schedule.controller;

import com.gnims.project.domain.schedule.dto.*;
import com.gnims.project.domain.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping("/schedules")
    public ResponseEntity<SimpleScheduleResult> createSchedule(@RequestBody ScheduleForm scheduleForm) {
        SimpleScheduleResult result = scheduleService.makeSchedule(scheduleForm);
        return new ResponseEntity<>(result, HttpStatus.valueOf(result.getStatus()));
    }

    //스케줄 전체 조회
    @GetMapping("/users/{user-id}/events")
    public ResponseEntity<ReadScheduleResult> readAllScheduleV2(@PathVariable("user-id") Long userId) {
        List<ReadAllResponse> responses = scheduleService.readAllSchedule(userId);
        return new ResponseEntity<>(new ReadScheduleResult<>(200, "전체 조회 완료", responses), HttpStatus.OK);
    }

}