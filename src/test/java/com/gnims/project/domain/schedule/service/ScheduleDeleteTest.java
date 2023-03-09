package com.gnims.project.domain.schedule.service;

import com.gnims.project.domain.event.entity.Event;
import com.gnims.project.domain.event.repository.EventRepository;
import com.gnims.project.domain.notification.repository.NotificationRepository;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import static com.gnims.project.share.message.ExceptionMessage.*;
import static com.gnims.project.share.message.ResponseMessage.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
public class ScheduleDeleteTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Autowired
    EventRepository eventRepository;

    TransactionStatus status = null;
    String hostToken = null;
    String userToken = null;

    @BeforeEach
    void beforeEach() throws Exception {
        makeUser();

        MvcResult result1 = getLoginResult();
        hostToken = result1.getResponse().getHeader("Authorization");

        MvcResult result2 = getLoginResult2();
        userToken = result2.getResponse().getHeader("Authorization");

        Long id1 = userRepository.findByNickname("딸기").get().getId();
        Long id2 = userRepository.findByNickname("당근").get().getId();
        Long id3 = userRepository.findByNickname("수박").get().getId();

        //when
        mvc.perform(post("/events").header("Authorization", hostToken)
                .contentType(APPLICATION_JSON)
                .content("{\"date\": \"2023-03-15\", " +
                        "\"time\":\"16:00:00\"," +
                        "\"subject\":\"자바 스터디\"," +
                        "\"content\":\"람다, 스트림을 공부합니다.\", " +
                        "\"participantsId\": " +
                        "[" + id1 + "," + id2 + "," + id3 + "]}"));
    }

    @AfterEach
    void afterEach() {
        notificationRepository.deleteAll();
        scheduleRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }


    @DisplayName("주최자(일정을 만든 사람)가 일정 삭제 시 " +
            "상태 코드 200, message : {DELETE_SCHEDULE_MESSAGE} 반환" +
            "event 엔티티 isDeleted 필드 false -> true 변경")
    @Test
    void 일정삭제_성공_케이스() throws Exception {
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);

        Event event = eventRepository.findBySubject("자바 스터디").get();

        //when
        mvc.perform(MockMvcRequestBuilders.delete("/events/" + event.getId())
                .header("Authorization", hostToken))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(DELETE_SCHEDULE_MESSAGE));

        Event updateEvent = eventRepository.findBySubject("자바 스터디").get();
        //then
        Assertions.assertThat(updateEvent.getIsDeleted()).isTrue();
    }

    @DisplayName("주최자가 아닌 사람이 일정을 삭제할 경우 " +
            "상태 코드 403, message : {ALREADY_PROCESSED_OR_NO_AUTHORITY_SCHEDULE} 반환" +
            "event 엔티티 isDeleted 필드 false 유지")
    @Test
    void 일정삭제_실패_케이스1() throws Exception {
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);

        Event event = eventRepository.findBySubject("자바 스터디").get();

        //when
        mvc.perform(MockMvcRequestBuilders.delete("/events/" + event.getId())
                        .header("Authorization", userToken))
                //then
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ALREADY_PROCESSED_OR_NO_AUTHORITY_SCHEDULE));

        Event updateEvent = eventRepository.findBySubject("자바 스터디").get();
        //then
        Assertions.assertThat(updateEvent.getIsDeleted()).isFalse();
    }

    @DisplayName("존재하지 않는 일정을 삭제할 경우 " +
            "상태 코드 403, message : {ALREADY_PROCESSED_OR_NO_AUTHORITY_SCHEDULE} 반환" +
            "event 엔티티 isDeleted 필드 false 유지")
    @Test
    void 일정삭제_실패_케이스2() throws Exception {
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);

        //when
        mvc.perform(MockMvcRequestBuilders.delete("/events/" + Integer.MAX_VALUE)
                        .header("Authorization", userToken))
                //then
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ALREADY_PROCESSED_OR_NO_AUTHORITY_SCHEDULE));
    }

    @DisplayName("이미 삭제된 일정을 삭제하려는 경우 " +
            "상태 코드 400, message : {이미 삭제된 일정입니다} 반환" +
            "event 엔티티 isDeleted 필드 true -> true 유지")
    @Test
    void 일정삭제_이미_삭제된_케이스() throws Exception {
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);

        Event event = eventRepository.findBySubject("자바 스터디").get();

        //when
        mvc.perform(MockMvcRequestBuilders.delete("/events/" + event.getId())
                        .header("Authorization", hostToken));

        //when - 이미 삭제된 일정을 삭제하려고 하는 경우
        mvc.perform(MockMvcRequestBuilders.delete("/events/" + event.getId())
                        .header("Authorization", hostToken))
                //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value( "이미 삭제된 일정입니다."));

        Event updateEvent = eventRepository.findBySubject("자바 스터디").get();
        //then
        Assertions.assertThat(updateEvent.getIsDeleted()).isTrue();
    }

    private MvcResult getLoginResult() throws Exception {
        MvcResult result = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\",\"socialCode\": \"AUTH\" }")).andReturn();
        return result;
    }

    private MvcResult getLoginResult2() throws Exception {
        return mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"suback@gmail.com\", \"password\": \"123456aA9\",\"socialCode\": \"AUTH\" }")).andReturn();

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

}
