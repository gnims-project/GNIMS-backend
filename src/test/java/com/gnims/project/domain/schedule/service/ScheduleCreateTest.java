package com.gnims.project.domain.schedule.service;

import com.gnims.project.domain.event.entity.Event;
import com.gnims.project.domain.event.repository.EventRepository;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.List;
import java.util.stream.Collectors;

import static com.gnims.project.domain.schedule.entity.ScheduleStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
class ScheduleCreateTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    PlatformTransactionManager transactionManager;

    TransactionStatus status = null;
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
        scheduleRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("공동 일정 생성 시 이벤트 초대 수락한 사람만 스케줄을 조회할 수 있어야 한다. " +
            "스케줄 미수락(PENDING, REJECT)일 경우 스케줄이 조회되지 않는다. ")
    @Test
    void 스케줄_조회_조건1() throws Exception {
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);

        //given
        Long hostId = userRepository.findByNickname("딸기").get().getId();
        Long inviteeId1 = userRepository.findByNickname("당근").get().getId();

        //when
        mvc.perform(post("/events").header("Authorization", hostToken)
                        .contentType(APPLICATION_JSON)
                        .content("{\"date\": \"2023-03-15\", " +
                                "\"time\":\"16:00:00\"," +
                                "\"subject\":\"과일 정기 모임\"," +
                                "\"content\":\"방이동 채소가게에서 저녁 식사\", " +
                                "\"participantsId\": " +
                                "[" + hostId + "," + inviteeId1 + "]}"));

        //then - 호스트는 이벤트 자동 수락이기 때문에 조회되는 일정 1개
        mvc.perform(get("/v2-dto/users/"+ hostId +"/events").header("Authorization", inviteeToken))
                .andExpect(jsonPath("$.data.size()").value(1));

        //then - 이벤트를 수락하지 않았기 때문에 초대받은 유저는 조회되는 일정 0개
        mvc.perform(get("/v2-dto/users/"+ inviteeId1 +"/events").header("Authorization", inviteeToken))
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
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);

        //given
        String expression = "$.[?(@.message == '%s')]";
        Long hostId = userRepository.findByNickname("딸기").get().getId();
        Long inviteeId1 = userRepository.findByNickname("당근").get().getId();
        Long inviteeId2 = userRepository.findByNickname("수박").get().getId();

        //when
        mvc.perform(post("/events").header("Authorization", hostToken)
                .contentType(APPLICATION_JSON)
                .content("{\"date\": \"2023-03-15\", " +
                        "\"time\":\"16:00:00\"," +
                        "\"subject\":\"과일 정기 모임\"," +
                        "\"content\":\"방이동 채소가게에서 저녁 식사\", " +
                        "\"participantsId\": " +
                        "[" + hostId + "," + inviteeId1 + "," + inviteeId2 + "]}"))
                //then
                .andExpect(jsonPath(expression, "스케줄 생성 완료").exists());


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

    private MvcResult getMvcResult(String content) throws Exception {
        MvcResult result = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)).andReturn();
        return result;
    }
}