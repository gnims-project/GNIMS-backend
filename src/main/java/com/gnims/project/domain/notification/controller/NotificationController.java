package com.gnims.project.domain.notification.controller;

import com.gnims.project.domain.notification.dto.NotificationResponseDto;
import com.gnims.project.domain.notification.service.NotificationService;
import com.gnims.project.security.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
    *       MIME TYPE - text/event-stream 형태로 받아야함.
     *      클라이어트로부터 오는 알림 구독 요청을 받는다.
     *      로그인한 유저는 SSE 연결
     *      lAST_EVENT_ID = 이전에 받지 못한 이벤트가 존재하는 경우 [ SSE 시간 만료 혹은 종료 ]
     *      전달받은 마지막 ID 값을 넘겨 그 이후의 데이터[ 받지 못한 데이터 ]부터 받을 수 있게 한다
     */

    @GetMapping(value ="/connect" , produces = "text/event-stream")
    public ResponseEntity<SseEmitter> subscribe(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                @RequestHeader(value="Last-Event-ID",required = false,defaultValue = "")
                                String lastEventId){
        SseEmitter sseEmitter = notificationService.subscribe(userDetails.receiveUserId(), lastEventId);
        
        return ResponseEntity.ok(sseEmitter);
    }

    //알림조회
    @GetMapping(value = "/notifications")
    public List<NotificationResponseDto> findAllNotifications(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return notificationService.findAllNotifications(userDetails.receiveUserId());
    }

    //전체목록 알림 조회에서 해당 목록 클릭 시 읽음처리 ,
    @GetMapping("/notifications/read/{notificationId}")
    public void readNotification(@PathVariable Long notificationId){
        notificationService.readNotification(notificationId);

    }
}
