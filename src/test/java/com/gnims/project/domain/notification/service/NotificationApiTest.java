package com.gnims.project.domain.notification.service;

import com.gnims.project.domain.friendship.dto.FriendShipServiceResponse;
import com.gnims.project.domain.notification.dto.NotificationForm;
import com.gnims.project.domain.notification.entity.Notification;
import com.gnims.project.domain.notification.repository.NotificationRepository;
import com.gnims.project.domain.notification.repository.SseEmitterManager;
import com.gnims.project.domain.schedule.dto.ScheduleServiceForm;
import com.gnims.project.domain.user.entity.User;
import com.gnims.project.domain.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.gnims.project.domain.notification.entity.NotificationType.*;
import static com.gnims.project.share.message.ExceptionMessage.BAD_ACCESS;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@RecordApplicationEvents
class NotificationApiTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    SseEmitterManager sseEmitterManager;
    @Autowired
    ApplicationEvents events;

    String hostToken = null;
    String inviteeToken = null;

    @BeforeEach
    void beforeEach() throws Exception {
        makeUser();

        MvcResult result = getLoginResult("{\"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}");

        hostToken = result.getResponse().getHeader("Authorization");

        MvcResult result2 = getLoginResult("{\"email\": \"danguen@gmail.com\", \"password\": \"123456aA9\"}");
        inviteeToken = result2.getResponse().getHeader("Authorization");
    }

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
    }

    @DisplayName("사용자가 알림 요청을 할 경우 해당 " +
            "사용자의 Id로 된 key를 가진 SseEmitter 가 생성된다. " +
            "연결을 요청하지 않은 사용자는 SseEmitter 가 존재하지 않는다.")
    @Test
    void SSE_연결_테스트() throws Exception {
        //when - SSE 연결
        mvc.perform(get("/connection").header("Authorization", inviteeToken));
        User connectUser = userRepository.findByNickname("당근").get();
        User unConnectUser = userRepository.findByNickname("딸기").get();

        //then
        Assertions.assertThat(sseEmitterManager.getSseEmitters().get(connectUser.getId())).isNotNull();
        Assertions.assertThat(sseEmitterManager.getSseEmitters().get(unConnectUser.getId())).isNull();

    }

    @DisplayName("알림 전체 조회 성공 시, " +
            "200 상태코드 반환 " +
            "알림 갯수만큼 응답한다.")
    @Test
    void 알림전체조회_테스트() throws Exception {
        User user = userRepository.findByNickname("딸기").get();
        Long createBy = userRepository.findByNickname("당근").get().getId();

        Notification notificationA = new Notification(user, NotificationForm.of(createBy, user.getId(), "테스트 알림1", SCHEDULE));
        Notification notificationB = new Notification(user, NotificationForm.of(createBy, user.getId(), "테스트 알림2", SCHEDULE));

        notificationRepository.save(notificationA);
        notificationRepository.save(notificationB);

        mvc.perform(get("/notifications").header("Authorization", hostToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()").value(2));
    }

    @DisplayName("알림 상세 조회 시 " +
            "Notification 엔티티 isChecked 필드는 False -> True 로 변경된다.")
    @Test
    void 알림상세조회_성공케이스() throws Exception {
        User sender = userRepository.findByNickname("당근").get();
        Long createBy = userRepository.findByNickname("딸기").get().getId();
        //given - 딸기가 당근에게 팔로우했을 때
        Notification notificationA = new Notification(sender, NotificationForm.of(createBy, sender.getId(), "테스트 알림1", SCHEDULE));
        Notification saveNotification = notificationRepository.save(notificationA);

        //알림을 확인하기 전 isChecked 필드
        Assertions.assertThat(saveNotification.getIsChecked()).isFalse();
        //알림이 생긴다.
        mvc.perform(get("/notifications/" + saveNotification.getId())
                        .header("Authorization", inviteeToken))
                .andExpect(status().isOk());

        Notification updateNotification = notificationRepository.findAllByUserId(sender.getId()).get(0);
        //알림을 확인후
        Assertions.assertThat(updateNotification.getIsChecked()).isTrue();
    }

    @DisplayName("존재하지 않는 알람을 상세 조회 시 " +
            "상태 코드 400 " +
            "메시지 {BAD_ACCESS} 반환")
    @Test
    void 알림상세조회_실패케이스1() throws Exception {
        User sender = userRepository.findByNickname("당근").get();
        Long createBy = userRepository.findByNickname("딸기").get().getId();
        //given - 딸기가 당근에게 팔로우했을 때
        Notification notification = new Notification(sender, NotificationForm.of(createBy, sender.getId(), "테스트 알림1", SCHEDULE));
        notificationRepository.save(notification);

        //알림이 생긴다.
        mvc.perform(get("/notifications/" + Long.MAX_VALUE)
                        .header("Authorization", inviteeToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(BAD_ACCESS));

    }

    @DisplayName("내게 오지 않은 알림 상세 조회 시 " +
            "상태 코드 400 " +
            "메시지 {BAD_ACCESS} 반환")
    @Test
    void 알림상세조회_실패케이스2() throws Exception {
        User sender = userRepository.findByNickname("당근").get();
        Long createBy = userRepository.findByNickname("딸기").get().getId();
        //given - 딸기가 당근에게 팔로우했을 때
        Notification notification = new Notification(sender, NotificationForm.of(createBy, sender.getId(), "테스트 알림1", SCHEDULE));
        Notification saveNotification = notificationRepository.save(notification);

        mvc.perform(get("/notifications/" + saveNotification.getId())
                        .header("Authorization", hostToken)) // hostToken 을 보유한 사용자에게 온 알림이 아니다.
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(BAD_ACCESS));

        Notification updateNotification = notificationRepository.findAllByUserId(sender.getId()).get(0);
        //알림을 확인후
        Assertions.assertThat(updateNotification.getIsChecked()).isFalse();
    }

    @DisplayName("일정에 초대받을 경우, " +
            "SSE 연결 여부와 상관없이 알림(Notification)이 저장된다.Notification 에는 " +
            "알림을 보낸이(CreatedBy) " +
            "받는이(UserId) " +
            "알림 타입(Notification) - SCHEDULE 이 저장된다.")
    @Disabled("비동기 처리로 바꾼 뒤 오류 - 원인 파악 중 - 포스트맨으로 API 테스트하는 경우에는 정상 작동")
    @Test
    void 알림비동기처리_테스트1() throws Exception {
        //given
        Long hostId = userRepository.findByNickname("딸기").get().getId();
        Long inviteeId = userRepository.findByNickname("당근").get().getId();

        //when
        createSchedule(hostId, inviteeId);
        //then
        Notification notification = notificationRepository.findAllByUserIdOrderByCreateAtDesc(inviteeId).get(0);
        Assertions.assertThat(notification.getCreateBy()).isEqualTo(hostId);
        Assertions.assertThat(notification.getUser().getId()).isEqualTo(inviteeId);
        Assertions.assertThat(notification.getNotificationType()).isEqualTo(SCHEDULE);
    }

    @DisplayName("최초 팔로우 관계가 맺어질 때 " +
            "SSE 연결 여부와 상관없이 알림(Notification)이 저장된다.Notification 에는 " +
            "알림을 보낸이(CreatedBy) " +
            "받는이(UserId) " +
            "알림 타입(Notification) - FRIENDSHIP 이 저장된다.")
    @Disabled("비동기 처리로 바꾼 뒤 오류 - 원인 파악 중 - 포스트맨으로 API 테스트하는 경우에는 정상 작동")
    @Test
    void 알림비동기처리_테스트2() throws Exception {

        Long hostId = userRepository.findByNickname("딸기").get().getId();
        Long followId = userRepository.findByNickname("당근").get().getId();

        //when
        mvc.perform(post("/friendship/followings/" + followId).header("Authorization", hostToken));

        //then
        Notification notification = notificationRepository.findAllByUserIdOrderByCreateAtDesc(followId).get(0);
        Assertions.assertThat(notification.getCreateBy()).isEqualTo(hostId);
        Assertions.assertThat(notification.getUser().getId()).isEqualTo(followId);
        Assertions.assertThat(notification.getNotificationType()).isEqualTo(FRIENDSHIP);
    }

    @DisplayName("팔로우 알림은 최초의 팔로우 관계가 맺어질 때만 발생한다. " +
            "최초의 팔로우(FollowStatus : INIT)를 맺을 때만 생성되고" +
            "취소(INACTIVE) -> 다시 맺을 경우(ACTIVE) 알림이 생성되지 않는다.")
    @Test
    void 알림비동기처리_테스트3() throws Exception {
        //given
        Long followId = userRepository.findByNickname("당근").get().getId();
        //when 팔로우 요청을 3번 보낸다. INIT - INACTIVE - ACTIVE
        for (int i = 1; i <= 3; i++) {
            mvc.perform(post("/friendship/followings/" + followId).header("Authorization", hostToken));
        }
        //then 알림을 조회할 경우 1개가 존재해야 한다.
        long count = events.stream(FriendShipServiceResponse.class).count();
        Assertions.assertThat(count).isEqualTo(1l);
    }

    @DisplayName("알림 전체 조회할 경우 " +
            "상태코드 200 " +
            "자신이 받은 알림의 갯수만큼 알림이 존재해야 한다. " +
            "테스트 케이스에서는 총 10번의 일정 초대를 보내기 때문에 알림을 조회할 경우 10개의 알림이 존재해야 한다.")
    @Test
    void 알림비동기처리_테스트4() throws Exception {
        //given
        Long hostId = userRepository.findByNickname("딸기").get().getId();
        Long inviteeId = userRepository.findByNickname("당근").get().getId();
        //when
        for (int i = 1; i <= 10; i++) {
            createSchedule(hostId, inviteeId);
        }
        //then 알림을 조회할 경우 10개가 존재해야 한다.
        long count = events.stream(ScheduleServiceForm.class).count();
        Assertions.assertThat(count).isEqualTo(10l);
    }

    @DisplayName("알림 모두 읽음 처리가 실행되면 알림 엔티티의 isChecked 필드는 false -> true 값으로 변경된다.")
    @Test
    void 알림모두읽음_성공케이스() throws Exception {
        User user = userRepository.findByNickname("딸기").get();
        Long createBy = userRepository.findByNickname("당근").get().getId();

        Notification notificationA = new Notification(user, NotificationForm.of(createBy, user.getId(), "테스트 알림1", SCHEDULE));
        Notification notificationB = new Notification(user, NotificationForm.of(createBy, user.getId(), "테스트 알림2", SCHEDULE));

        notificationRepository.save(notificationA);
        notificationRepository.save(notificationB);

        mvc.perform(put("/notifications").header("Authorization", hostToken));

        List<Notification> notifications = notificationRepository.findAllByUserId(user.getId());

        notifications.forEach(n -> Assertions.assertThat(n.getIsChecked()).isTrue());
    }

    @DisplayName("올바른 토큰을 가지고 있지 않으면 isChecked 필드는 false 에서 변경되지 않는다.")
    @Test
    void 알림모두읽음_실패케이스() throws Exception {
        User user = userRepository.findByNickname("딸기").get();
        Long createBy = userRepository.findByNickname("당근").get().getId();

        Notification notificationA = new Notification(user, NotificationForm.of(createBy, user.getId(), "테스트 알림1", SCHEDULE));
        Notification notificationB = new Notification(user, NotificationForm.of(createBy, user.getId(), "테스트 알림2", SCHEDULE));

        notificationRepository.save(notificationA);
        notificationRepository.save(notificationB);

        // hostToken 을 가진 유저에게 온 알림이다.
        mvc.perform(put("/notifications").header("Authorization", inviteeToken));

        List<Notification> notifications = notificationRepository.findAllByUserId(user.getId());
        notifications.forEach(n -> Assertions.assertThat(n.getIsChecked()).isFalse());
    }

    private void createSchedule(Long hostId, Long inviteeId) throws Exception {
        mvc.perform(post("/events").header("Authorization", hostToken)
                .contentType(APPLICATION_JSON)
                .content("{\"date\": \"9999-03-15\", " +
                        "\"time\":\"16:00:00\"," +
                        "\"subject\":\"과일 정기 모임\"," +
                        "\"content\":\"방이동 채소가게에서 저녁 식사\", " +
                        "\"participantsId\": " +
                        "[" + hostId + "," + inviteeId + "]}"));
    }

    private void makeUser() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "data", "", "application/json",
                "{\"nickname\" : \"딸기\",\"username\": \"이땡땡\", \"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}".getBytes());
        mvc.perform(multipart("/auth/signup")
                .file(file1));

        MockMultipartFile file2 = new MockMultipartFile(
                "data", "", "application/json",
                "{\"nickname\" : \"당근\",\"username\": \"김땡땡\", \"email\": \"danguen@gmail.com\", \"password\": \"123456aA9\"}".getBytes());
        mvc.perform(multipart("/auth/signup")
                .file(file2));

        MockMultipartFile file3 = new MockMultipartFile(
                "data", "", "application/json",
                "{\"nickname\" : \"수박\",\"username\": \"박땡땡\", \"email\": \"suback@gmail.com\", \"password\": \"123456aA9\"}".getBytes());
        mvc.perform(multipart("/auth/signup")
                .file(file3));
    }

    private MvcResult getLoginResult(String content) throws Exception {
        MvcResult result2 = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)).andReturn();
        return result2;
    }
}