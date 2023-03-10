package com.gnims.project.domain.schedule.service;

import com.gnims.project.domain.event.repository.EventRepository;
import com.gnims.project.domain.notification.repository.NotificationRepository;
import com.gnims.project.domain.schedule.dto.ReadAllScheduleDto;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
public class SchedulePastTest {

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

    @Autowired
    PlatformTransactionManager transactionManager;

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
        notificationRepository.deleteAll();
        scheduleRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }


    @DisplayName("?????? ????????? ?????? ?????? ??? " +
            "???????????? 200 " +
            "?????? ????????? ???????????? ????????????.")
    @Test
    void ??????_?????????_??????_??????1() throws Exception {
        //given
        Long hostId = userRepository.findByNickname("??????").get().getId();

        //?????? ?????? ??????
        makeSchedule(hostId, "\"2000-02-10\"");
        makeSchedule(hostId, "\"9999-02-10\"");

        mvc.perform(get("/events/past").header("Authorization", hostToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()").value(1));

    }

    @DisplayName("?????? ????????? ?????? ?????? ??? " +
            "???????????? 200 " +
            "?????? ????????? ????????? ????????? ???????????????.")
    @Test
    void ??????_?????????_??????_??????2() throws Exception {
        //given
        Long hostId = userRepository.findByNickname("??????").get().getId();

        List<String> stringDate = List.of("\"2000-03-13\"", "\"2000-02-12\"", "\"2000-04-14\"");
        //?????? ?????? ??????
        for (String date : stringDate) {
            makeSchedule(hostId, date);
        }


        List<ReadAllScheduleDto> eventAllQueries = scheduleRepository.readPastSchedule(hostId);
        LocalDate firstScheduleDate = eventAllQueries.get(0).getDate();
        LocalDate secondScheduleDate = eventAllQueries.get(1).getDate();
        LocalDate thirdScheduleDate = eventAllQueries.get(2).getDate();

        Assertions.assertThat(firstScheduleDate).isEqualTo("2000-04-14");
        Assertions.assertThat(secondScheduleDate).isEqualTo("2000-03-13");
        Assertions.assertThat(thirdScheduleDate).isEqualTo("2000-02-12");

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

    private MvcResult getLoginResult(String content) throws Exception {
        MvcResult result2 = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)).andReturn();
        return result2;
    }

    private void makeSchedule(Long hostId, String date) throws Exception {
        mvc.perform(post("/events").header("Authorization", hostToken)
                        .contentType(APPLICATION_JSON)
                        .content("{\"date\":" + date + ", " +
                                "\"time\":\"12:00:00\"," +
                                "\"subject\":\"?????? ?????? ??????\"," +
                                "\"content\":\"?????????????????? ?????? ??????\", " +
                                "\"participantsId\": " +
                                "[" + hostId + "]}"))
                .andExpect(status().isCreated());
    }
}
