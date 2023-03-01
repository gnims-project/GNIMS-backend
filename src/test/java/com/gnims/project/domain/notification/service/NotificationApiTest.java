package com.gnims.project.domain.notification.service;

import com.gnims.project.domain.notification.entity.Notification;
import com.gnims.project.domain.notification.repository.NotificationRepository;
import com.gnims.project.domain.notification.repository.SseEmitterManager;
import com.gnims.project.domain.user.entity.User;
import com.gnims.project.domain.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static com.gnims.project.domain.notification.entity.NotificationType.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NotificationApiTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    SseEmitterManager sseEmitterManager;

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
    void test1() throws Exception {
        //when - SSE 연결
        mvc.perform(get("/connect").header("Authorization", inviteeToken));
        User connectUser = userRepository.findByNickname("당근").get();
        User unConnectUser = userRepository.findByNickname("딸기").get();

        //then
        Assertions.assertThat(sseEmitterManager.getSseEmitters().get(connectUser.getId())).isNotNull();
        Assertions.assertThat(sseEmitterManager.getSseEmitters().get(unConnectUser.getId())).isNull();

    }

    @DisplayName("일정에 초대받을 경우, " +
            "SSE 연결 여부와 상관없이 알림(Notification)이 저장된다.Notification 에는 " +
            "알림을 보낸이(CreatedBy) " +
            "받는이(UserId) " +
            "알림 타입(Notification) - SCHEDULE 이 저장된다.")
    @Test
    void test2() throws Exception {
        //given
        Long hostId = userRepository.findByNickname("딸기").get().getId();
        Long inviteeId = userRepository.findByNickname("당근").get().getId();

        //when
        createSchedule(hostId, inviteeId);

        //then
        Notification notification = notificationRepository.findAllByUser_Id(inviteeId).get(0);
        Assertions.assertThat(notification.getCreateBy()).isEqualTo(hostId);
        Assertions.assertThat(notification.getUser().getId()).isEqualTo(inviteeId);
        Assertions.assertThat(notification.getNotificationType()).isEqualTo(SCHEDULE);
    }

    @DisplayName("최초 팔로우 관계가 맺어질 때 " +
            "SSE 연결 여부와 상관없이 알림(Notification)이 저장된다.Notification 에는 " +
            "알림을 보낸이(CreatedBy) " +
            "받는이(UserId) " +
            "알림 타입(Notification) - FRIENDSHIP 이 저장된다.")
    @Test
    void test3() throws Exception {

        Long hostId = userRepository.findByNickname("딸기").get().getId();
        Long followId = userRepository.findByNickname("당근").get().getId();

        //when
        mvc.perform(post("/friendship/followings/" + followId).header("Authorization", hostToken));

        //then
        Notification notification = notificationRepository.findAllByUser_Id(followId).get(0);
        Assertions.assertThat(notification.getCreateBy()).isEqualTo(hostId);
        Assertions.assertThat(notification.getUser().getId()).isEqualTo(followId);
        Assertions.assertThat(notification.getNotificationType()).isEqualTo(FRIENDSHIP);
    }

    @DisplayName("알림 상세 조회 시 " +
            "Notification 엔티티 isChecked 필드는 False -> True 로 변경된다.")
    @Test
    void test4() throws Exception {
        Long followId = userRepository.findByNickname("당근").get().getId();
        //given - 딸기가 당근에게 팔로우했을 때
        mvc.perform(post("/friendship/followings/" + followId).header("Authorization", hostToken));

        //알림이 생긴다.
        Notification notification = notificationRepository.findAllByUser_Id(followId).get(0);

        //알림을 확인하기 전 isChecked 필드
        Assertions.assertThat(notification.getIsChecked()).isFalse();

        //when - 알림 확인
        mvc.perform(get("/notifications/" + notification.getId())
                .header("Authorization", inviteeToken))
                .andExpect(status().isOk());

        Notification updateNotification = notificationRepository.findAllByUser_Id(followId).get(0);
        //알림을 확인 후 isChecked 필드
        Assertions.assertThat(updateNotification.getIsChecked()).isTrue();
    }

    @DisplayName("팔로우 알림은 최초의 팔로우 관계가 맺어질 때만 발생한다. " +
            "최초의 팔로우(FollowStatus : INIT)를 맺을 때만 생성되고" +
            "취소(INACTIVE) -> 다시 맺을 경우(ACTIVE) 알림이 생성되지 않는다.")
    @Test
    void test5() throws Exception {
        //given
        Long followId = userRepository.findByNickname("당근").get().getId();
        //when 팔로우 요청을 3번 보낸다. INIT - INACTIVE - ACTIVE
        for (int i = 1; i <= 3; i++) {
            mvc.perform(post("/friendship/followings/" + followId).header("Authorization", hostToken));
        }
        //then 알림을 조회할 경우 10개가 존재해야 한다.
        mvc.perform(get("/notifications").header("Authorization", inviteeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()").value(1));
    }

    @DisplayName("알림 전체 조회할 경우 " +
            "상태코드 200 " +
            "자신이 받은 알림의 갯수만큼 알림이 존재해야 한다. " +
            "테스트 케이스에서는 총 10번의 일정 초대를 보내기 때문에 알림을 조회할 경우 10개의 알림이 존재해야 한다.")
    @Test
    void test6() throws Exception {
        //given
        Long hostId = userRepository.findByNickname("딸기").get().getId();
        Long inviteeId = userRepository.findByNickname("당근").get().getId();
        //when
        for (int i = 1; i <= 10; i++) {
            createSchedule(hostId, inviteeId);
        }
        //then 알림을 조회할 경우 10개가 존재해야 한다.
        mvc.perform(get("/notifications").header("Authorization", inviteeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()").value(10));
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