package com.gnims.project.domain.schedule.service;

import com.gnims.project.domain.event.entity.Event;
import com.gnims.project.domain.event.repository.EventRepository;
import com.gnims.project.domain.schedule.repository.ScheduleRepository;
import com.gnims.project.domain.user.entity.User;
import com.gnims.project.domain.user.repository.UserRepository;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
public class ScheduleReadTest {

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
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\" : \"딸기\",\"username\": \"이땡땡\", \"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\", \"socialCode\" : \"AUTH\"}"));

        mvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\" : \"당근\",\"username\": \"김땡땡\", \"email\": \"danguen@gmail.com\", \"password\": \"123456aA9\", \"socialCode\" : \"AUTH\"}"));

        mvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\" : \"수박\",\"username\": \"박땡땡\", \"email\": \"suback@gmail.com\", \"password\": \"123456aA9\", \"socialCode\" : \"AUTH\"}"));

        MvcResult result = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\",\"socialCode\": \"AUTH\" }")).andReturn();

        token = result.getResponse().getHeader("Authorization");

        Long id1 = userRepository.findByNickname("딸기").get().getId();
        Long id2 = userRepository.findByNickname("당근").get().getId();


        //when
        mvc.perform(post("/events").header("Authorization", token)
                .contentType(APPLICATION_JSON)
                .content("{\"date\": \"2023-03-15\", " +
                        "\"time\":\"16:00:00\"," +
                        "\"subject\":\"자바 스터디\"," +
                        "\"content\":\"람다, 스트림에 대해 공부합니다.\", " +
                        "\"cardColor\": \"pink\"," +
                        "\"participantsId\": " +
                        "[" + id1 + "," + id2 + "]}"));
    }

    @AfterEach
    void afterEach() {
        scheduleRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("일정 상세 조회 시, 초대 받은 사람의 username 반환")
    @Test
    void test1() throws Exception {
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);

        Event event = eventRepository.findBySubject("자바 스터디").get();
        Long eventId = event.getId();


        mvc.perform(MockMvcRequestBuilders.get("/users/events/" + eventId)
                .header("Authorization", token)
                .contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data[?(@.subject == '%s')]","자바 스터디").exists())
                .andExpect(jsonPath("$.data.invitees[?(@.username == '%s')]", "이땡땡", "김땡땡").exists())
                .andExpect(jsonPath("$.data.invitees[?(@.username == '%s')]", "박땡땡").doesNotExist());
    }

    @DisplayName("일정 전체 조회 시, 초대 받은 사람의 username, profile 반환")
    @Test
    void test2() throws Exception {
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);

        User user = userRepository.findByUsername("이땡땡").get();
        Long userId = user.getId();


        mvc.perform(MockMvcRequestBuilders.get("/users/" +userId + "/events")
                        .header("Authorization", token)
                        .contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data[?(@.subject == '%s')]","자바 스터디").exists())
                .andExpect(jsonPath("$..invitees[?(@.username == '%s')]", "이땡땡", "김땡땡").exists())
                .andExpect(jsonPath("$..invitees[?(@.username == '%s')]", "박땡땡").doesNotExist())
                .andExpect(jsonPath("$..invitees[?(@.profile == '%s')]", "대충 프로필 URI").exists());
    }

    @DisplayName("일정 전체 조회 시, 응답 결과의 data 길이 자신이 가진 일정 갯수와 동일해야 한다.")
    @Test
    void test3() throws Exception {
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);

        User user = userRepository.findByNickname("딸기").get();
        Long userId = user.getId();

        mvc.perform(post("/events").header("Authorization", token)
                .contentType(APPLICATION_JSON)
                .content("{\"date\": \"2023-03-15\", " +
                        "\"time\":\"16:00:00\"," +
                        "\"subject\":\"스프링 스터디\"," +
                        "\"content\":\"스프링 MVC를 공부합니다.\", " +
                        "\"cardColor\": \"black\"," +
                        "\"participantsId\": " +
                        "[" + userId + "]}"));

        mvc.perform(MockMvcRequestBuilders.get("/users/" +userId + "/events")
                        .header("Authorization", token)
                        .contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data.size()", 2).exists());

    }
}
