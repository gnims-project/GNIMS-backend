package com.gnims.project.domain.schedule.service;

import com.gnims.project.domain.event.entity.Event;
import com.gnims.project.domain.event.repository.EventRepository;
import com.gnims.project.domain.notification.repository.NotificationRepository;
import com.gnims.project.domain.schedule.repository.ScheduleRepository;
import com.gnims.project.domain.user.entity.User;
import com.gnims.project.domain.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class ScheduleReadOneApiTest {
    @Autowired
    MockMvc mvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ScheduleRepository scheduleRepository;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    EventRepository eventRepository;

    String hostToken = null;
    String invitedToken = null;
    String nonInvitedToken = null;

    @BeforeEach
    void beforeEach() throws Exception {
        makeUser();

        MvcResult result = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}")).andReturn();

        hostToken = result.getResponse().getHeader("Authorization");

        MvcResult result2 = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"danguen@gmail.com\", \"password\": \"123456aA9\"}")).andReturn();

        invitedToken = result2.getResponse().getHeader("Authorization");

        MvcResult result3 = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"suback@gmail.com\", \"password\": \"123456aA9\"}")).andReturn();

        nonInvitedToken = result3.getResponse().getHeader("Authorization");

        Long hostId = userRepository.findByNickname("딸기").get().getId();
        Long inviteeId = userRepository.findByNickname("당근").get().getId();

        //when
        mvc.perform(post("/events").header("Authorization", hostToken)
                .contentType(APPLICATION_JSON)
                .content("{\"date\": \"9999-03-15\", " +
                        "\"time\":\"16:00:00\"," +
                        "\"subject\":\"자바 스터디\"," +
                        "\"content\":\"람다, 스트림에 대해 공부합니다.\", " +
                        "\"cardColor\": \"pink\"," +
                        "\"participantsId\": " +
                        "[" + hostId + "," + inviteeId + "]}"));
    }

    @AfterEach
    void afterEach() {
        notificationRepository.deleteAll();
        scheduleRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * 기본적으로 일정을 생성한 사람(주최자)은 일정(Schedule)에 참석(isAccepted = true)한다고 결정하였습니다.
     */
    @DisplayName("일정 상세 조회 시, " +
            "응답 결과로 초대를 수락한(isAccepted) 사람의 username 반환 " +
            "초대를 수락하지 않은 사람의 username 은 반환하지 않습니다." +
            "일정 제목, 본문, 카드 색상, 날짜, 시간, 디데이 반환")
    @Test
    void test1() throws Exception {

        Event event = eventRepository.findBySubject("자바 스터디").get();
        Long eventId = event.getId();

        Long hostId = userRepository.findByNickname("딸기").get().getId();

        mvc.perform(get("/events/" + eventId)
                        .header("Authorization", hostToken)
                        .contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data[?(@.subject == '%s')]","자바 스터디").exists())
                .andExpect(jsonPath("$.data[?(@.content == '%s')]","람다, 스트림에 대해 공부합니다.").exists())
                .andExpect(jsonPath("$.data[?(@.cardColor == '%s')]","pink").exists())
                .andExpect(jsonPath("$.data[?(@.date == '%s')]","9999-03-15").exists())
                .andExpect(jsonPath("$.data[?(@.time == '%s')]","16:00:00").exists())
                .andExpect(jsonPath("$..dday").isNotEmpty())
                .andExpect(jsonPath("$.data.invitees[?(@.username == '%s')]", "이땡땡").exists())
                .andExpect(jsonPath("$.data.invitees[?(@.username == '%s')]", "박땡땡").doesNotExist())
                .andExpect(jsonPath("$.data.invitees[?(@.username == '%s')]", "김땡땡").doesNotExist())
                .andExpect(jsonPath("$..hostId").value(hostId.intValue()));
    }

    @DisplayName("팔로우 관계가 아닌 상태에서 상대방의 일정 상세 조회를 할 경우 " +
            "403 상태코드를 반환하고 " +
            "일정과 관련된 응답 내역은 존재하지 않는다.")
    @Test
    void test2() throws Exception {
        Event event = eventRepository.findBySubject("자바 스터디").get();
        Long eventId = event.getId();

        mvc.perform(get("/events/" + eventId).header("Authorization", nonInvitedToken))
                .andExpect(status().isForbidden());
    }

    @DisplayName("일정을 만든 사람이 아닌 일정에 참여 중인 사람과 팔로우 관계가 맺어져 있다면 상세 조회 시" +
            "상태 코드 200" +
            "데이터가 포함된 채로 정상 응답 한다. ")
    @Test
    void test3() throws Exception {

        Long eventId = eventRepository.findBySubject("자바 스터디").get().getId();

        //대기중인 일정 -> 일정에 참여
        Long userId = userRepository.findByNickname("당근").get().getId();
        mvc.perform(post( "/events/" + eventId + "/acceptance")
                .header("Authorization", invitedToken)
                .contentType(APPLICATION_JSON));

        mvc.perform(post("/friendship/followings/"+ userId).header("Authorization", nonInvitedToken));

        mvc.perform(get("/events/" + eventId).header("Authorization", nonInvitedToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..subject").value("자바 스터디"));
    }

    @DisplayName("일정을 만든 사람과 팔로우 관계가 맺어져 있다면 상세 조회 시 " +
            "상태 코드 200" +
            "데이터가 포함된 채로 정상 응답 한다. ")
    @Test
    void test4() throws Exception {

        Long eventId = eventRepository.findBySubject("자바 스터디").get().getId();

        //대기중인 일정 -> 일정에 참여
        Long userId = userRepository.findByNickname("딸기").get().getId();
        mvc.perform(post( "/events/" + eventId + "/acceptance")
                .header("Authorization", hostToken)
                .contentType(APPLICATION_JSON));

        mvc.perform(post("/friendship/followings/"+ userId).header("Authorization", nonInvitedToken));

        mvc.perform(get("/events/" + eventId).header("Authorization", nonInvitedToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..subject").value("자바 스터디"));
    }

    @DisplayName("일정을 만든 사람과 팔로우 관계가 맺어지지 않더라도 본인이 포함되어 있으면 " +
            "상태 코드 200 " +
            "데이터가 포함된 채로 정상 응답 한다. ")
    @Test
    void test5() throws Exception {
        Event event = eventRepository.findBySubject("자바 스터디").get();
        Long eventId = event.getId();

        mvc.perform(post( "/events/" + eventId + "/acceptance")
                .header("Authorization", invitedToken)
                .contentType(APPLICATION_JSON));

        mvc.perform(get("/events/" + eventId).header("Authorization", invitedToken))
                .andExpect(status().isOk());
    }

    @DisplayName("일정을 수락하지 않았으면 " +
            "상태 코드 403 " +
            "데이터가 포함된 채로 정상 응답 한다. ")
    @Test
    void test6() throws Exception {
        Event event = eventRepository.findBySubject("자바 스터디").get();
        Long eventId = event.getId();

        mvc.perform(get("/events/" + eventId).header("Authorization", invitedToken))
                .andExpect(status().isForbidden());
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

        MockMultipartFile file4 = new MockMultipartFile(
                "data", "", "application/json",
                "{\"nickname\" : \"참외\",\"username\": \"최땡땡\", \"email\": \"chamwhe@gmail.com\", \"password\": \"123456aA9\"}".getBytes());
        mvc.perform(multipart("/auth/signup")
                .file(file4));
    }
}
