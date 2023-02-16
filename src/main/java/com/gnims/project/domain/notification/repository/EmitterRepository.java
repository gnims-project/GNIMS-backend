package com.gnims.project.domain.notification.repository;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;


/**
 * save - Emitter를 저장한다.
 * saveEventCache - 이벤트를 저장한다.
 * findAllEmitterStartWithByMemberId - 해당 회원과 관련된 모든 Emitter를 찾는다.
 * 브라우저당 여러 개 연결이 가능하기에 여러 Emitter가 존재할 수 있다.
 * findAllEventCacheStartWithByMemberId - 해당 회원과 관련된 모든 이벤트를 찾는다.
 * deleteById - Emitter를 지운다.
 * deleteAllEmitterStartWithId - 해당 회원과 관련된 모든 Emitter를 지운다.
 * deleteAllEventCacheStartWithId - 해당 회원과 관련된 모든 이벤트를 지운다.*
 */
public interface EmitterRepository {
    //TODO: interface로 변경한 이유는 추후 확정성을 고려했기 때문이다
    SseEmitter save(String emitterId, SseEmitter sseEmitter);
    void saveEventCache(String emitterId, Object event);
    Map<String, SseEmitter> findAllEmitterStartWithByUserId(String userId);
    Map<String,Object> findAllEventCacheStartWithByUserId(String userId);
    void deleteById(String id);

    void deleteAllEmitterStartWithId(String userId);

    void deleteAllEventCacheStartWithId(String userId);




}
