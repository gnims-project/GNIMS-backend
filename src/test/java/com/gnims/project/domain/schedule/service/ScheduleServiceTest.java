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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@AutoConfigureMockMvc
@SpringBootTest
class ScheduleServiceTest {

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
    String token = null;

    @BeforeEach
    void beforeEach() throws Exception {
        mvc.perform(post("/auth/signup")
                .contentType(APPLICATION_JSON)
                .content("{\"nickname\" : \"딸기\", \"email\": \"ddalgi@gmail.com\", \"password\": \"123456Aa9\"}"));

        mvc.perform(post("/auth/signup")
                .contentType(APPLICATION_JSON)
                .content("{\"nickname\" : \"당근\", \"email\": \"danguen@gmail.com\", \"password\": \"123456Aa9\"}"));

        mvc.perform(post("/auth/signup")
                .contentType(APPLICATION_JSON)
                .content("{\"nickname\" : \"수박\", \"email\": \"suback@gmail.com\", \"password\": \"123456Aa9\"}"));

        MvcResult result = mvc.perform(post("/auth/login")
                .contentType(APPLICATION_JSON)
                .content("{\"email\": \"ddalgi@gmail.com\", \"password\": \"123456Aa9\"}")).andReturn();

        token = result.getResponse().getHeader("Authorization");
    }

    @AfterEach
    void afterEach() {
        scheduleRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("이벤트를 만들고 일정을 잡으면 - message 필드 - '일정 조회 완료', 생성된 Schedule 엔티티는 초대된 사용자의 id 값을 포함하고 있다.")
    @Test
    void test1() throws Exception {
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);

        //given
        String expression = "$.[?(@.message == '%s')]";
        Long id1 = userRepository.findByUsername("딸기").get().getId();
        Long id2 = userRepository.findByUsername("당근").get().getId();
        Long id3 = userRepository.findByUsername("수박").get().getId();

        //when
        mvc.perform(post("/schedule").header("Authorization", token)
                .contentType(APPLICATION_JSON)
                .content("{\"date\": \"2023-03-15\", " +
                        "\"time\":\"16:00:00\"," +
                        "\"subject\":\"과일 정기 모임\"," +
                        "\"content\":\"방이동 채소가게에서 저녁 식사\", " +
                        "\"participantsId\": " +
                        "[" + id1 + "," + id2 + "," + id3 + "]}"))
                //then
                .andExpect(MockMvcResultMatchers.jsonPath(expression, "일정 조회 완료").exists());


        List<Schedule> schedules = scheduleRepository.findAll();
        Event event = schedules.get(0).getEvent();
        List<Long> userIds = schedules.stream().map(s -> s.getUser().getId()).collect(Collectors.toList());
        Event findEvent = eventRepository.findBySubject("과일 정기 모임").get();

        //then - 생성된 Schedule에는 초대된 사용자 id 들이 포함되어 있다.
        Assertions.assertThat(userIds).contains(id1, id2, id3);
        Assertions.assertThat(event.getContent()).isEqualTo(findEvent.getContent());

    }
}