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

        Long id1 = userRepository.findByNickname("??????").get().getId();
        Long id2 = userRepository.findByNickname("??????").get().getId();
        Long id3 = userRepository.findByNickname("??????").get().getId();

        //when
        mvc.perform(post("/events").header("Authorization", hostToken)
                .contentType(APPLICATION_JSON)
                .content("{\"date\": \"2023-03-15\", " +
                        "\"time\":\"16:00:00\"," +
                        "\"subject\":\"?????? ?????????\"," +
                        "\"content\":\"??????, ???????????? ???????????????.\", " +
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


    @DisplayName("?????????(????????? ?????? ??????)??? ?????? ?????? ??? " +
            "?????? ?????? 200, message : {DELETE_SCHEDULE_MESSAGE} ??????" +
            "event ????????? isDeleted ?????? false -> true ??????")
    @Test
    void ????????????_??????_?????????() throws Exception {
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);

        Event event = eventRepository.findBySubject("?????? ?????????").get();

        //when
        mvc.perform(MockMvcRequestBuilders.delete("/events/" + event.getId())
                .header("Authorization", hostToken))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(DELETE_SCHEDULE_MESSAGE));

        Event updateEvent = eventRepository.findBySubject("?????? ?????????").get();
        //then
        Assertions.assertThat(updateEvent.getIsDeleted()).isTrue();
    }

    @DisplayName("???????????? ?????? ????????? ????????? ????????? ?????? " +
            "?????? ?????? 403, message : {ALREADY_PROCESSED_OR_NO_AUTHORITY_SCHEDULE} ??????" +
            "event ????????? isDeleted ?????? false ??????")
    @Test
    void ????????????_??????_?????????1() throws Exception {
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);

        Event event = eventRepository.findBySubject("?????? ?????????").get();

        //when
        mvc.perform(MockMvcRequestBuilders.delete("/events/" + event.getId())
                        .header("Authorization", userToken))
                //then
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ALREADY_PROCESSED_OR_NO_AUTHORITY_SCHEDULE));

        Event updateEvent = eventRepository.findBySubject("?????? ?????????").get();
        //then
        Assertions.assertThat(updateEvent.getIsDeleted()).isFalse();
    }

    @DisplayName("???????????? ?????? ????????? ????????? ?????? " +
            "?????? ?????? 403, message : {ALREADY_PROCESSED_OR_NO_AUTHORITY_SCHEDULE} ??????" +
            "event ????????? isDeleted ?????? false ??????")
    @Test
    void ????????????_??????_?????????2() throws Exception {
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);

        //when
        mvc.perform(MockMvcRequestBuilders.delete("/events/" + Integer.MAX_VALUE)
                        .header("Authorization", userToken))
                //then
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ALREADY_PROCESSED_OR_NO_AUTHORITY_SCHEDULE));
    }

    @DisplayName("?????? ????????? ????????? ??????????????? ?????? " +
            "?????? ?????? 400, message : {?????? ????????? ???????????????} ??????" +
            "event ????????? isDeleted ?????? true -> true ??????")
    @Test
    void ????????????_??????_?????????_?????????() throws Exception {
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);

        Event event = eventRepository.findBySubject("?????? ?????????").get();

        //when
        mvc.perform(MockMvcRequestBuilders.delete("/events/" + event.getId())
                        .header("Authorization", hostToken));

        //when - ?????? ????????? ????????? ??????????????? ?????? ??????
        mvc.perform(MockMvcRequestBuilders.delete("/events/" + event.getId())
                        .header("Authorization", hostToken))
                //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value( "?????? ????????? ???????????????."));

        Event updateEvent = eventRepository.findBySubject("?????? ?????????").get();
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
                "{\"nickname\" : \"??????\",\"username\": \"?????????\", \"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}".getBytes());
        mvc.perform(multipart("/auth/signup")
                .file(file1));

        MockMultipartFile file2 = new MockMultipartFile(
                "data", "", "application/json",
                "{\"nickname\" : \"??????\",\"username\": \"?????????\", \"email\": \"danguen@gmail.com\", \"password\": \"123456aA9\"}".getBytes());
        mvc.perform(multipart("/auth/signup")
                .file(file2));

        MockMultipartFile file3 = new MockMultipartFile(
                "data", "", "application/json",
                "{\"nickname\" : \"??????\",\"username\": \"?????????\", \"email\": \"suback@gmail.com\", \"password\": \"123456aA9\"}".getBytes());
        mvc.perform(multipart("/auth/signup")
                .file(file3));
    }

}
