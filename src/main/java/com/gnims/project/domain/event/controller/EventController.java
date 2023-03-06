package com.gnims.project.domain.event.controller;

import com.gnims.project.domain.event.service.EventService;
import com.gnims.project.domain.schedule.dto.SimpleScheduleResult;
import com.gnims.project.domain.schedule.dto.UpdateForm;
import com.gnims.project.security.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.gnims.project.share.message.ResponseMessage.DELETE_SCHEDULE_MESSAGE;
import static com.gnims.project.share.message.ResponseMessage.UPDATE_SCHEDULE_MESSAGE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.ResponseEntity.*;

@RestController
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    //스케줄 삭제
    @DeleteMapping("/events/{event-id}")
    public ResponseEntity<SimpleScheduleResult> deleteSchedule(@PathVariable("event-id") Long eventId,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {

        eventService.softDeleteSchedule(userDetails.receiveUserId(), eventId);
        return ok(new SimpleScheduleResult(200, DELETE_SCHEDULE_MESSAGE));
    }

    //스케줄 수정
    @PutMapping("/events/{event-id}")
    public ResponseEntity<SimpleScheduleResult> updateSchedule(@PathVariable("event-id") Long eventId,
                                                               @RequestBody @Valid UpdateForm updateForm,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {

        eventService.updateSchedule(userDetails.receiveUserId(), updateForm, eventId);
        return ok(new SimpleScheduleResult(200, UPDATE_SCHEDULE_MESSAGE));
    }
}
