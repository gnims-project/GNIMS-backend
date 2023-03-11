package com.gnims.project.domain.notification.repository;

import com.gnims.project.domain.notification.entity.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static org.springframework.http.MediaType.*;

@Slf4j
@Component
public class SseEmitterManager {

    private static final Long TIMEOUT = 30 * 1000l; // 10분
    private final Map<Long, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

    public SseEmitter save(Long userId) {
        SseEmitter sseEmitter = new SseEmitter(TIMEOUT);
        this.sseEmitters.put(userId, sseEmitter);
        log.info("new emitter added : {}", sseEmitter);
        log.info("emitters size : {}", sseEmitters.size());

        /**
         * 타임아웃이 발생하면 브라우저에서 재연결 요청을 보내는데
         * 이때 새로운 Emitter 객체를 생성하기 때문에 기존 Emitter 를 제거해야 한다.
         */
        sseEmitter.onCompletion(() -> this.sseEmitters.remove(userId));
        sseEmitter.onTimeout(() -> sseEmitter.complete());
        return sseEmitter;
    }

    public void sendInitMessage(SseEmitter sseEmitter, String username) {
        try {
            sseEmitter.send(SseEmitter.event()
                    .name("connect")
                    .data("안녕하세요! " + username + "님 반갑습니다!"));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(SseEmitter sseEmitter, NotificationType notificationType, Object message) throws IOException {
        sseEmitter.send(SseEmitter.event()
                .name(notificationType.getEventType())
                .data(message, APPLICATION_JSON));
    }

    public Map<Long, SseEmitter> getSseEmitters() {
        return sseEmitters;
    }

    public void clear() {
        sseEmitters.clear();
    }
}
