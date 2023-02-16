package com.gnims.project.domain.notification.service;

import com.gnims.project.domain.notification.dto.NotificationResponseDto;
import com.gnims.project.domain.notification.entity.AlarmType;
import com.gnims.project.domain.notification.entity.Notification;
import com.gnims.project.domain.notification.repository.EmitterRepository;
import com.gnims.project.domain.notification.repository.EmitterRepositoryImpl;
import com.gnims.project.domain.notification.repository.NotificationRepository;
import com.gnims.project.domain.user.entity.User;
import com.gnims.project.security.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
    구독 -> Spring 에서 제공하는 Emitter를 생성 후 저장
    -> 구독자가 생성한 Emitter를 불러와 이벤트에 대한 응답을 전송
    -> 어떤 회원이 어떤 Emitter를 사용하느지에 대한 구분이 필요
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final EmitterRepository emitterRepository;
    private final NotificationRepository notificationRepository;

    public SseEmitter subscribe(Long userId, String lastEventId) {
        //emitter 하나하나 에 고유의 값을 주기 위해
        String emitterId = makeTimeIncludeId(userId);

//        Long timeout = 60L * 1000L * 60L; // 1시간
        // 생성된 emiiterId를 기반으로 emitter를 저장
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter());

        //emitter의 시간이 만료된 후 레포에서 삭제
        emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
        emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));

        // 503 에러를 방지하기 위해 처음 연결 진행 시 더미 데이터를 전달
        String eventId = makeTimeIncludeId(userId);
        // 수 많은 이벤트 들을 구분하기 위해 이벤트 ID에 시간을 통해 구분을 해줌
        sendNotification(emitter, eventId, emitterId, "EventStream Created. [userId=" + userId + "]");

        // 클라이언트가 미수신한 Event 목록이 존재할 경우 전송하여 Event 유실을 예방
        if (hasLostData(lastEventId)) {
            sendLostData(lastEventId, userId, emitterId, emitter);
        }

        return emitter;
    }


    // SseEmitter를 구분 -> 구분자로 시간을 사용함
    // 시간을 붙혀주는 이유 -> 브라우저에서 여러개의 구독을 진행 시
    //탭 마다 SssEmitter 구분을 위해 시간을 붙여 구분하기 위해 아래와 같이 진행
    private String makeTimeIncludeId(Long userId) {
        return userId + "_" + System.currentTimeMillis();
    }

    // 유효시간이 다 지난다면 503 에러가 발생하기 때문에 더미데이터를 발행
    private void sendNotification(SseEmitter emitter, String eventId, String emitterId, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(eventId)
                    .data(data));
        } catch (IOException exception) {
            emitterRepository.deleteById(emitterId);
            log.error("sse 연결오류!!!", exception);
        }
    }
    // Last - event - id 가 존재한다는 것은 받지 못한 데이터가 있다는 것이다.
    private boolean hasLostData(String lastEventId) {
        return !lastEventId.isEmpty();
    }

    // 받지못한 데이터가 있다면 last - event - id를 기준으로 그 뒤의 데이터를 추출해 알림을 보내주면 된다.
    private void sendLostData(String lastEventId, Long userId, String emitterId, SseEmitter emitter) {
        Map<String, Object> eventCaches = emitterRepository.findAllEventCacheStartWithByUserId(String.valueOf(userId));
        eventCaches.entrySet().stream()
                .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                .forEach(entry -> sendNotification(emitter, entry.getKey(), emitterId, entry.getValue()));
    }

    /**
        : 실제 다른 사용자가 알림을 보낼 수 있는 기능이 필요
        알림을 구성 후 해당 알림에 대한 이벤트를 발생
        -> 어떤 회원에게 알림을 보낼지에 대해 찾고 알림을
        받을 회원의 emitter들을 모두 찾아 해당 emitter를 Send
     */

    @Async
    public void send(User receiver, AlarmType alarmType, String message, Long articlesId, String title, LocalDateTime createdAt) {
//        여기 createdAt은 댓글 생성될때 찍히는시간,
        Notification notification = notificationRepository.save(createNotification(receiver, alarmType, message, articlesId, title));
        log.info("DB 메시지 저장 확인 : {}", message);
        String receiverId = String.valueOf(receiver.getId());
        String eventId = receiverId + "_" + System.currentTimeMillis();
        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithByUserId(receiverId);
        emitters.forEach(
                (key, emitter) -> {
                    emitterRepository.saveEventCache(key, notification);
                    sendNotification(emitter, eventId, key, NotificationResponseDto.create(notification));
                }
        );
    }



    private Notification createNotification(User receiver, AlarmType alarmType, String message,
                                            Long articlesId, String title) {

        return Notification.builder()
                .receiver(receiver)
                .alarmType(alarmType)
                .message(message)
                .articlesId(articlesId)
                .title(title)
                .readState(false) // 현재 읽음상태
                .build();
    }


    @Transactional
    public List<NotificationResponseDto> findAllNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findAllByReceiver_UserId(userId);
//        for (Notification notification : notifications) {
//            notification.changeState();
//        }
        return notifications.stream()
                .map(NotificationResponseDto::create)
                .collect(Collectors.toList());
    }

    @Transactional
    public void readNotification(Long notificationId) {
        //알림을 받은 사람의 id 와 알림의 id 를 받아와서 해당 알림을 찾는다.
        Optional<Notification> notification = notificationRepository.findById(notificationId);
        Notification checkNotification = notification.orElseThrow(()-> new IllegalArgumentException("존재하지 않는 알람입니다."));
        checkNotification.changeState(); // 읽음처리

    }

    @Transactional
    public void deleteAllByNotifications(UserDetailsImpl userDetails) {
        Long receiverId = userDetails.getUser().getId();
        notificationRepository.deleteAllByUser_Id(receiverId);

    }
    @Transactional
    public void deleteByNotifications(Long notificationId) {
        if (notificationId == null) {
            throw new IllegalArgumentException("존재하지 않는 알람입니다.");
        }
        notificationRepository.deleteById(notificationId);
    }
}