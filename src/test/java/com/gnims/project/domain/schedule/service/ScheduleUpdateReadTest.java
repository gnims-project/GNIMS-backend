package com.gnims.project.domain.schedule.service;

import com.gnims.project.domain.event.entity.Event;
import com.gnims.project.domain.event.repository.EventRepository;
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

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
public class ScheduleUpdateReadTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

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

        mvc.perform(post("/events").header("Authorization", hostToken)
                .contentType(APPLICATION_JSON)
                .content("{\"date\": \"2023-03-15\", " +
                        "\"time\":\"16:00:00\"," +
                        "\"subject\":\"자바 스터디\", " +
                        "\"cardColor\":\"pink\" ," +
                        "\"content\":\"람다, 스트림을 공부합니다.\", " +
                        "\"participantsId\": " +
                        "[" + id1 + "," + id2 + "," + id3 + "]}"));
    }

    @AfterEach
    void afterEach() {
        scheduleRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("주최자가 일정을 수정하는 경우" +
            "상태 코드 200, 응답 메시지{스케줄을 수정합니다.} 반환 " +
            "수정한 내역이 event 엔티티에 반영")
    @Test
    void 스케줄_수정_성공_케이스() throws Exception {
        //given
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);

        Event event = eventRepository.findBySubject("자바 스터디").get();
        mvc.perform(put("/events/" + event.getId())
                        .header("Authorization", hostToken)
                        .contentType(APPLICATION_JSON)
                        .content("{\"date\": \"2023-03-15\", " +
                                "\"time\":\"16:00:00\"," +
                                "\"cardColor\":\"pink\"," +
                                "\"subject\":\"자바스크립트 스터디\"," +
                                "\"content\":\"비동기 처리를 공부합니다.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("스케줄을 수정합니다."));

        Event updateEvent = eventRepository.findById(event.getId()).get();

        Assertions.assertThat(updateEvent.getSubject()).isEqualTo("자바스크립트 스터디");
        Assertions.assertThat(updateEvent.getContent()).isEqualTo("비동기 처리를 공부합니다.");
    }

    /**
     * 현재 일부 수정할 경우 수정하지 않은 데이터가 NULL 처리되는 현상
     * @throws Exception
     */

    @DisplayName("주최자가 일정의 일부를 수정하는 경우" +
            "상태 코드 200, 응답 메시지{스케줄을 수정합니다.} 반환 " +
            "수정하지 않은 내역은 기존과 동일해야 한다.")
    @Test
    void 스케줄_수정_성공_케이스2() throws Exception {
        //given
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);

        Event event = eventRepository.findBySubject("자바 스터디").get();
        mvc.perform(put("/events/" + event.getId())
                        .header("Authorization", hostToken)
                        .contentType(APPLICATION_JSON)
                        .content("{\"subject\": \"CPP 스터디\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("스케줄을 수정합니다."));

        Event updateEvent = eventRepository.findById(event.getId()).get();

//        Assertions.assertThat(updateEvent.getCardColor()).isEqualTo("pink");
//        Assertions.assertThat(updateEvent.getIsDeleted()).isEqualTo(false);
//        Assertions.assertThat(updateEvent.getAppointment().getDate()).isEqualTo("2023-03-15");
//        Assertions.assertThat(updateEvent.getAppointment().getTime()).isEqualTo("16:00:00");

    }

    @DisplayName("주최자가 아닌 유저가 스케줄을 수정하려는 경우 " +
            "상태 코드 403, 응답 메시지{수정 권한이 없습니다.} 반환" +
            "event 엔티티의 내용은 수정 전과 동일해야 한다.")
    @Test
    void 스케줄_수정_실패_케이스() throws Exception {
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);

        Event originalEvent = eventRepository.findBySubject("자바 스터디").get();

        mvc.perform(put("/events/" + originalEvent.getId())
                .header("Authorization", userToken)
                .contentType(APPLICATION_JSON)
                .content("{\"date\": \"2023-03-15\", " +
                        "\"time\":\"16:00:00\"," +
                        "\"cardColor\":\"pink\"," +
                        "\"subject\":\"자바스크립트 스터디\"," +
                        "\"content\":\"비동기 처리를 공부합니다.\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("수정 권한이 없습니다."));

        Event updateEvent = eventRepository.findBySubject("자바 스터디").get();
        Assertions.assertThat(updateEvent.getModifiedAt()).isEqualTo(originalEvent.getModifiedAt());
    }

    @DisplayName("주최자가 이미 삭제된 스케줄을 수정하려는 경우 " +
            "상태 코드 400, 응답 메시지{이미 삭제된 스케줄입니다.} 반환")
    @Test
    void 스케줄_수정_실패_케이스2() throws Exception {

        //given
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);

        Event originalEvent = eventRepository.findBySubject("자바 스터디").get();

        mvc.perform(MockMvcRequestBuilders.delete("/events/" + originalEvent.getId())
                .header("Authorization", hostToken));

        mvc.perform(put("/events/" + originalEvent.getId())
                        .header("Authorization", hostToken)
                        .contentType(APPLICATION_JSON)
                        .content("{\"date\": \"2023-03-15\", " +
                                "\"time\":\"16:00:00\"," +
                                "\"cardColor\":\"pink\"," +
                                "\"subject\":\"자바스크립트 스터디\"," +
                                "\"content\":\"비동기 처리를 공부합니다.\"}"))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("존재하지 않는 일정을 수정하려는 경우 " +
            "상태 코드 403, 응답 메시지{수정 권한이 없습니다.} 반환")
    @Test
    void 스케줄_수정_실패_케이스3() throws Exception {
        //given
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);

        mvc.perform(put("/events/" + Integer.MAX_VALUE)
                        .header("Authorization", hostToken)
                        .contentType(APPLICATION_JSON)
                        .content("{\"date\": \"2023-03-15\", " +
                                "\"time\":\"16:00:00\"," +
                                "\"cardColor\":\"pink\"," +
                                "\"subject\":\"자바스크립트 스터디\"," +
                                "\"content\":\"비동기 처리를 공부합니다.\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("수정 권한이 없습니다."));
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
