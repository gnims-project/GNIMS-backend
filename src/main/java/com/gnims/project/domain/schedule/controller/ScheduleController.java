package com.gnims.project.domain.schedule.controller;

import com.gnims.project.domain.schedule.dto.*;
import com.gnims.project.domain.schedule.service.ScheduleService;
import com.gnims.project.security.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Transactional
public class ScheduleController {

    private final ScheduleService scheduleService;

    //스케줄 전체 조회 기본 버전 - user-id 붙인 이유는 타인의 일정도 볼 수 있어야 하기 때문에
    @GetMapping("/users/{user-id}/events")
    public ResponseEntity<ReadScheduleResult> readAllSchedule(@PathVariable("user-id") Long followId) {
        List<ReadAllResponse> responses = scheduleService.readAllSchedule(followId);
        return new ResponseEntity<>(new ReadScheduleResult<>(200, "전체 조회 완료", responses), HttpStatus.OK);
    }

    //스케줄 전체 조회 최적화 진행중 - user <- fetch join // event batch-size
    @GetMapping("/v2/users/{user-id}/events")
    public ResponseEntity<ReadScheduleResult> readAllScheduleV2(@PathVariable("user-id") Long followId) {
        List<ReadAllResponse> responses = scheduleService.readAllScheduleV2(followId);
        return new ResponseEntity<>(new ReadScheduleResult<>(200, "전체 조회 완료", responses), HttpStatus.OK);
    }

    //스케줄 전체 조회 한방 쿼리 DTO
    @GetMapping("/v2-dto/users/{user-id}/events")
    public ResponseEntity<ReadScheduleResult> readAllScheduleV2Dto(@PathVariable("user-id") Long followId) {
        List<ReadAllResponse> responses = scheduleService.readAllScheduleV2Dto(followId);
        return new ResponseEntity<>(new ReadScheduleResult<>(200, "전체 조회 완료", responses), HttpStatus.OK);
    }

    //스케줄 전체 조회 최적화 진행중 - Paging 버전
    @GetMapping("/v2-page/users/{user-id}/events")
    public ResponseEntity<ReadScheduleResult> readAllScheduleV2Page(@PathVariable("user-id") Long followId,
                                                                    @RequestParam Integer page,
                                                                    @RequestParam Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        List<ReadAllResponse> responses = scheduleService.readAllScheduleV2Pageable(followId, pageRequest);
        return new ResponseEntity<>(new ReadScheduleResult<>(200, "전체 조회 완료", responses), HttpStatus.OK);
    }

    //스케줄 단건 조회 - 기본 버전
    @GetMapping("/events/{event-id}")
    public ResponseEntity<ReadScheduleResult> readOneSchedule(@PathVariable("event-id") Long eventId) {
        ReadOneResponse response = scheduleService.readOneSchedule(eventId);
        return new ResponseEntity<>(new ReadScheduleResult(200, "상세 조회 완료", response), HttpStatus.OK);
    }

    //스케줄 단건 조회 - join fetch
    @GetMapping("/v2/users/{user-id}/events/{event-id}")
    public ResponseEntity<ReadScheduleResult> readOneScheduleV2(@PathVariable("user-id") Long userId,
                                                                @PathVariable("event-id") Long eventId) {
        ReadOneResponse response = scheduleService.readOneScheduleV2(userId, eventId);
        return new ResponseEntity<>(new ReadScheduleResult(200, "상세 조회 완료", response), HttpStatus.OK);
    }
    //스케줄 단건 조회 - 쿼리 최적화 한방 쿼리 DTO 반환
    @GetMapping("/v2/events/{event-id}")
    public ResponseEntity<ReadScheduleResult> readOneScheduleV2DTO(@PathVariable("event-id") Long eventId) {
        ReadOneResponse response = scheduleService.readOneScheduleV2DTO(eventId);
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
    public ResponseEntity<SimpleScheduleResult> readPendingSchedule(@PathVariable("event-id") Long eventId,
                                                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
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
                                                               @RequestBody @Valid UpdateForm updateForm,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {

        scheduleService.updateSchedule(userDetails.receiveUserId(), updateForm, eventId);
        return new ResponseEntity<>(new SimpleScheduleResult(200, "스케줄을 수정합니다."), HttpStatus.OK);
    }
}