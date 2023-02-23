package com.gnims.project.domain.notification.controller;

import com.gnims.project.domain.notification.repository.SseEmitterManager;
import com.gnims.project.security.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;



@Slf4j
@RestController
@RequiredArgsConstructor
@Transactional
public class NotificationController {

    private final SseEmitterManager sseEmitterManager;

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        SseEmitter sseEmitter = sseEmitterManager.save(userDetails.receiveUserId());
        sseEmitterManager.sendInitMessage(sseEmitter, userDetails.getUsername());

        return sseEmitter;
    }
}

