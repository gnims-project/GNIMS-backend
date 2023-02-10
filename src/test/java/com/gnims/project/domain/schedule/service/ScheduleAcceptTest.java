package com.gnims.project.domain.schedule.service;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@AutoConfigureMockMvc
@SpringBootTest
public class ScheduleAcceptTest {

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
        mvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\" : \"딸기\",\"username\": \"이딸기\", \"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\", \"socialCode\" : \"AUTH\"}"));

        mvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\" : \"당근\",\"username\": \"이당근\", \"email\": \"danguen@gmail.com\", \"password\": \"123456aA9\", \"socialCode\" : \"AUTH\"}"));

        mvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\" : \"수박\",\"username\": \"이수박\", \"email\": \"suback@gmail.com\", \"password\": \"123456aA9\", \"socialCode\" : \"AUTH\"}"));

        MvcResult result = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\", \"socialCode\" : \"AUTH\"}")).andReturn();

        hostToken = result.getResponse().getHeader("Authorization");

        MvcResult result2 = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"danguen@gmail.com\", \"password\": \"123456aA9\", \"socialCode\" : \"AUTH\"}")).andReturn();

        inviteeToken = result2.getResponse().getHeader("Authorization");
    }

    @AfterEach
    void afterEach() {
        scheduleRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }


    @DisplayName("일정 수락 시 " +
            "일정의 수락한 사용자의 Schedule 엔티티 isAccepted 필드 false -> true 변화" +
            "타인의 isAccepted 필드는 그대로 false 반환")
    @Test
    void test1() throws Exception {
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);

        //given
        Long hostId = userRepository.findByNickname("딸기").get().getId();
        Long inviteeId1 = userRepository.findByNickname("당근").get().getId();
        Long inviteeId2 = userRepository.findByNickname("수박").get().getId();

        mvc.perform(post("/events").header("Authorization", hostToken)
                .contentType(APPLICATION_JSON)
                .content("{\"date\": \"2023-03-15\", " +
                        "\"time\":\"12:00:00\"," +
                        "\"subject\":\"과일 정기 모임\"," +
                        "\"content\":\"채소가게에서 저녁 식사\", " +
                        "\"participantsId\": " +
                        "[" + hostId + "," + inviteeId1 + "," + inviteeId2 + "]}"));

        Long eventId = eventRepository.findBySubject("과일 정기 모임").get().getId();
        Schedule danguenSchedule = scheduleRepository.findByUser_IdAndEvent_Id(inviteeId1, eventId).get();
        Schedule subackSchedule = scheduleRepository.findByUser_IdAndEvent_Id(inviteeId2, eventId).get();


        // 스케줄을 수락하기 전 당근,수박의 isAccepted 필드
        Assertions.assertThat(danguenSchedule.getIsAccepted()).isFalse();
        Assertions.assertThat(subackSchedule.getIsAccepted()).isFalse();

        //when
        mvc.perform(post("/users/" + inviteeId1 + "/events/" + eventId + "/acceptance")
                .header("Authorization", inviteeToken)
                .contentType(APPLICATION_JSON));

        //then
        // 스케줄을 수락 후 당근, 수박의 isAccepted 필드
        Schedule updateDanguenSchedule = scheduleRepository.findByUser_IdAndEvent_Id(inviteeId1, eventId).get();
        Schedule updateSubackSchedule = scheduleRepository.findByUser_IdAndEvent_Id(inviteeId2, eventId).get();
        Assertions.assertThat(updateDanguenSchedule.getIsAccepted()).isTrue();
        Assertions.assertThat(updateSubackSchedule.getIsAccepted()).isFalse();
    }
}
