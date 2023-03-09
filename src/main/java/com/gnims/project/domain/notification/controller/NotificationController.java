package com.gnims.project.domain.notification.controller;

import com.gnims.project.domain.notification.dto.ReadNotificationResponse;
import com.gnims.project.domain.notification.dto.ReadNotificationResult;
import com.gnims.project.domain.notification.dto.SimpleNotificationResult;
import com.gnims.project.domain.notification.repository.SseEmitterManager;
import com.gnims.project.domain.notification.service.NotificationService;
import com.gnims.project.security.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static com.gnims.project.share.message.ResponseMessage.*;
import static org.springframework.http.ResponseEntity.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Transactional
public class NotificationController {

    private final SseEmitterManager sseEmitterManager;
    private final NotificationService notificationService;

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        SseEmitter sseEmitter = sseEmitterManager.save(userDetails.receiveUserId());
        sseEmitterManager.sendInitMessage(sseEmitter, userDetails.getUsername());

        return sseEmitter;
    }

    @GetMapping("/notifications")
    public ResponseEntity<ReadNotificationResult> readAll(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.receiveUserId();
        List<ReadNotificationResponse> responses = notificationService.readAll(userId);

        return ok(new ReadNotificationResult(200, READ_ALL_NOTIFICATION_MESSAGE, responses));
    }

    @PutMapping("/notifications")
    public ResponseEntity<SimpleNotificationResult> deleteAll(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.receiveUserId();
        notificationService.checkAll(userId);

        return ok(new SimpleNotificationResult(200, CHECK_ALL_NOTIFICATION_MESSAGE));
    }

    @GetMapping("/notifications/{notification-id}")
    public ResponseEntity<SimpleNotificationResult> readAndCheckOne(@PathVariable("notification-id") Long notificationId) {
        notificationService.readAndCheckNotification(notificationId);

        return ok(new SimpleNotificationResult(200, READ_ONE_NOTIFICATION_MESSAGE));
    }

}

