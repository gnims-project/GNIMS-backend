package com.gnims.project.domain.schedule.service;

import com.gnims.project.domain.event.entity.Event;
import com.gnims.project.domain.event.repository.EventRepository;
import com.gnims.project.domain.notification.repository.NotificationRepository;
import com.gnims.project.domain.schedule.entity.Schedule;
import com.gnims.project.domain.schedule.repository.ScheduleRepository;
import com.gnims.project.domain.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import static com.gnims.project.domain.schedule.entity.ScheduleStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class ScheduleCreateTest {

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
    String inviteeToken = null;


    @BeforeEach
    void beforeEach() throws Exception {
        makeUser();

        MvcResult result1 = getMvcResult("{\"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\",\"socialCode\": \"AUTH\" }");
        hostToken = result1.getResponse().getHeader("Authorization");

        MvcResult result2 = getMvcResult("{\"email\": \"danguen@gmail.com\", \"password\": \"123456aA9\",\"socialCode\": \"AUTH\" }");
        inviteeToken = result2.getResponse().getHeader("Authorization");
    }

    @AfterEach
    void afterEach() {
        notificationRepository.deleteAll();
        scheduleRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("공동 일정 생성 시 이벤트 초대 수락한 사람만 스케줄을 조회할 수 있어야 한다. " +
            "스케줄 미수락(PENDING, REJECT)일 경우 스케줄이 조회되지 않는다. ")
    @Test
    void 스케줄_조회_조건1() throws Exception {

        //given
        Long hostId = userRepository.findByNickname("딸기").get().getId();
        Long inviteeId1 = userRepository.findByNickname("당근").get().getId();

        //when
        mvc.perform(post("/events").header("Authorization", hostToken)
                        .contentType(APPLICATION_JSON)
                        .content("{\"date\": \"9999-03-15\", " +
                                "\"time\":\"16:00:00\"," +
                                "\"subject\":\"과일 정기 모임\"," +
                                "\"content\":\"방이동 채소가게에서 저녁 식사\", " +
                                "\"participantsId\": " +
                                "[" + hostId + "," + inviteeId1 + "]}"));

        //then - 호스트는 이벤트 자동 수락이기 때문에 조회되는 일정 1개
        mvc.perform(get("/users/"+ hostId +"/events").header("Authorization", hostToken))
                .andExpect(jsonPath("$.data.size()").value(1));

        //then - 이벤트를 수락하지 않았기 때문에 초대받은 유저는 조회되는 일정 0개
        mvc.perform(get("/users/"+ inviteeId1 +"/events").header("Authorization", inviteeToken))
                .andExpect(jsonPath("$.data.size()").value(0));

    }

    @DisplayName("이벤트를 만들고 일정을 잡으면 - " +
            "message 필드 -> '일정 조회 완료'" +
            "생성된 Schedule 엔티티는 초대한 user의 id 값 포함" +
            "스케줄 생성자는 Schedule 엔티티 ScheduleStatus 필드 ACCEPT" +
            "초대받은 사람들은 ScheduleStatus 필드 PENDING 여야 한다. " +
            "이벤트가 생성되면 dDay 필드는 null이 아니다.")
    @Test
    void test1() throws Exception {
        //given
        Long hostId = userRepository.findByNickname("딸기").get().getId();
        Long inviteeId1 = userRepository.findByNickname("당근").get().getId();
        Long inviteeId2 = userRepository.findByNickname("수박").get().getId();

        //when
        mvc.perform(post("/events").header("Authorization", hostToken)
                .contentType(APPLICATION_JSON)
                .content("{\"date\": \"9999-03-15\", " +
                        "\"time\":\"16:00:00\"," +
                        "\"subject\":\"과일 정기 모임\"," +
                        "\"content\":\"방이동 채소가게에서 저녁 식사\", " +
                        "\"participantsId\": " +
                        "[" + hostId + "," + inviteeId1 + "," + inviteeId2 + "]}"))
                //then
                .andExpect(jsonPath("$.message").value("스케줄 생성 완료"));


        List<Schedule> schedules = scheduleRepository.findAll();
        Schedule hostSchedule = schedules.stream().filter(s -> s.isAccepted()).findFirst().get();
        List<Long> userIds = schedules.stream().map(s -> s.getUser().getId()).collect(Collectors.toList());
        Event findEvent = eventRepository.findBySubject("과일 정기 모임")
                .orElseThrow(() -> new IllegalAccessException("알 수 없는 에러"));

        //then - 생성된 Schedule 엔티티는 초대한 userId 값 포함
        Assertions.assertThat(userIds).contains(hostId, inviteeId1, inviteeId2);
        Assertions.assertThat(findEvent.getContent()).isEqualTo("방이동 채소가게에서 저녁 식사");

        //then - 스케줄 생성자는 Schedule 엔티티 isAccepted 필드 true
        Assertions.assertThat(hostSchedule.receiveUserId()).isEqualTo(hostId);
        //then - 초대받은 사람들은 scheduleStatus 필드 PENDING 여야 한다.
        Schedule inviteeSchedule = schedules.stream().filter(s -> s.getScheduleStatus().equals(PENDING)).findFirst().get();
        Assertions.assertThat(inviteeSchedule.receiveUserId()).isIn(List.of(inviteeId1, inviteeId2));
        //then - 이벤트가 생성되면 dDay 필드는 null이 아니다.
        Assertions.assertThat(findEvent.getDDay()).isNotNull();
    }

    @DisplayName("일정은 최대 자신을 제외하고 5명 까지 초대가능하다. " +
            "초대 인원이 5명이 넘을 시 일정은 생성되지 않고 " +
            "400 에러가 발생한다.")
    @Test
    void test2() throws Exception {
        //given
        Long hostId = userRepository.findByNickname("딸기").get().getId();
        Long inviteeId1 = userRepository.findByNickname("당근").get().getId();
        Long inviteeId2 = userRepository.findByNickname("수박").get().getId();
        Long inviteeId3 = userRepository.findByNickname("참외").get().getId();
        Long inviteeId4 = userRepository.findByNickname("메론").get().getId();
        Long inviteeId5 = userRepository.findByNickname("김치").get().getId();
        Long inviteeId6 = userRepository.findByNickname("커피").get().getId();

        //when
        mvc.perform(post("/events").header("Authorization", hostToken)
                        .contentType(APPLICATION_JSON)
                        .content("{\"date\": \"9999-03-15\", " +
                                "\"time\":\"16:00:00\"," +
                                "\"subject\":\"과일 정기 모임\"," +
                                "\"content\":\"방이동 채소가게에서 저녁 식사\", " +
                                "\"participantsId\": " +
                                "[" + hostId + "," + inviteeId1 + "," + inviteeId2 + "," + inviteeId3 + "" +
                                "," + inviteeId4 + "," + inviteeId5 + "," + inviteeId6 + "]}"))
                //then
                .andExpect(status().isBadRequest());

        Assertions.assertThat(eventRepository.findBySubject("과일 정기 모임").isEmpty()).isTrue();
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

        MockMultipartFile file5 = new MockMultipartFile(
                "data", "", "application/json",
                "{\"nickname\" : \"메론\",\"username\": \"메땡땡\", \"email\": \"meron@gmail.com\", \"password\": \"123456aA9\"}".getBytes());
        mvc.perform(multipart("/auth/signup")
                .file(file5));

        MockMultipartFile file6 = new MockMultipartFile(
                "data", "", "application/json",
                "{\"nickname\" : \"김치\",\"username\": \"치땡땡\", \"email\": \"kimchi@gmail.com\", \"password\": \"123456aA9\"}".getBytes());
        mvc.perform(multipart("/auth/signup")
                .file(file6));

        MockMultipartFile file7 = new MockMultipartFile(
                "data", "", "application/json",
                "{\"nickname\" : \"커피\",\"username\": \"최땡땡\", \"email\": \"coffee@gmail.com\", \"password\": \"123456aA9\"}".getBytes());
        mvc.perform(multipart("/auth/signup")
                .file(file7));
    }

    private MvcResult getMvcResult(String content) throws Exception {
        MvcResult result = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)).andReturn();
        return result;
    }
}