package com.gnims.project.domain.schedule.controller;

import com.gnims.project.domain.schedule.dto.*;
import com.gnims.project.domain.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping("/schedules")
    public ResponseEntity<SimpleScheduleResult> createSchedule(@RequestBody ScheduleForm scheduleForm) {
        SimpleScheduleResult result = scheduleService.makeSchedule(scheduleForm);
        return new ResponseEntity<>(result, HttpStatus.valueOf(result.getStatus()));
    }


}