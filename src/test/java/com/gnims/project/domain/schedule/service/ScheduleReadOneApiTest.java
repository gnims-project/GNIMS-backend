package com.gnims.project.domain.schedule.service;

import com.gnims.project.domain.event.entity.Event;
import com.gnims.project.domain.event.repository.EventRepository;
import com.gnims.project.domain.notification.repository.NotificationRepository;
import com.gnims.project.domain.schedule.repository.ScheduleRepository;
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

import static com.gnims.project.share.message.ExceptionMessage.BAD_ACCESS;
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

        Long hostId = userRepository.findByNickname("??????").get().getId();
        Long inviteeId = userRepository.findByNickname("??????").get().getId();

        //when
        mvc.perform(post("/events").header("Authorization", hostToken)
                .contentType(APPLICATION_JSON)
                .content("{\"date\": \"9999-03-15\", " +
                        "\"time\":\"16:00:00\"," +
                        "\"subject\":\"?????? ?????????\"," +
                        "\"content\":\"??????, ???????????? ?????? ???????????????.\", " +
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
     * ??????????????? ????????? ????????? ??????(?????????)??? ??????(Schedule)??? ??????(isAccepted = true)????????? ?????????????????????.
     */
    @DisplayName("?????? ?????? ?????? ???, " +
            "?????? ????????? ????????? ?????????(isAccepted) ????????? username ?????? " +
            "????????? ???????????? ?????? ????????? username ??? ???????????? ????????????." +
            "?????? ??????, ??????, ?????? ??????, ??????, ??????, ?????????, ????????? ID, ????????? ?????? ????????? ID ??????")
    @Test
    void ??????_?????????1() throws Exception {

        Event event = eventRepository.findBySubject("?????? ?????????").get();
        Long eventId = event.getId();

        Long hostId = userRepository.findByNickname("??????").get().getId();

        mvc.perform(get("/events/" + eventId)
                        .header("Authorization", hostToken)
                        .contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data[?(@.subject == '%s')]","?????? ?????????").exists())
                .andExpect(jsonPath("$.data[?(@.content == '%s')]","??????, ???????????? ?????? ???????????????.").exists())
                .andExpect(jsonPath("$.data[?(@.cardColor == '%s')]","pink").exists())
                .andExpect(jsonPath("$.data[?(@.date == '%s')]","9999-03-15").exists())
                .andExpect(jsonPath("$.data[?(@.time == '%s')]","16:00:00").exists())
                .andExpect(jsonPath("$..dday").isNotEmpty())
                .andExpect(jsonPath("$.data.invitees[?(@.username == '%s')]", "?????????").exists())
                .andExpect(jsonPath("$.data.invitees[?(@.username == '%s')]", "?????????").doesNotExist())
                .andExpect(jsonPath("$.data.invitees[?(@.username == '%s')]", "?????????").doesNotExist())
                .andExpect(jsonPath("$..hostId").value(hostId.intValue()));
    }

    @DisplayName("????????? ?????? YES ?????? ?????? ???" +
            "?????? ?????? 200 " +
            "???????????? ????????? ?????? ?????? ?????? ??????. ")
    @Test
    void ??????_?????????2() throws Exception {

        Long eventId = eventRepository.findBySubject("?????? ?????????").get().getId();

        //???????????? ?????? -> ????????? ??????
        Long userId = userRepository.findByNickname("??????").get().getId();
        mvc.perform(post( "/events/" + eventId + "/acceptance")
                .header("Authorization", invitedToken)
                .contentType(APPLICATION_JSON));

        mvc.perform(post("/friendship/followings/"+ userId).header("Authorization", nonInvitedToken));

        mvc.perform(get("/events/" + eventId).header("Authorization", nonInvitedToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..subject").value("?????? ?????????"));
    }

    @DisplayName("????????? ?????? ????????? ????????? ????????? ????????? ????????? ?????? ?????? ??? " +
            "?????? ?????? 200" +
            "???????????? ????????? ?????? ?????? ?????? ??????. ")
    @Test
    void ??????_?????????3() throws Exception {

        Long eventId = eventRepository.findBySubject("?????? ?????????").get().getId();

        //???????????? ?????? -> ????????? ??????
        Long userId = userRepository.findByNickname("??????").get().getId();
        mvc.perform(post( "/events/" + eventId + "/acceptance")
                .header("Authorization", hostToken)
                .contentType(APPLICATION_JSON));

        mvc.perform(post("/friendship/followings/"+ userId).header("Authorization", nonInvitedToken));

        mvc.perform(get("/events/" + eventId).header("Authorization", nonInvitedToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..subject").value("?????? ?????????"));
    }

    @DisplayName("????????? ?????? ????????? ????????? ????????? ???????????? ???????????? ????????? ???????????? ????????? " +
            "?????? ?????? 200 " +
            "???????????? ????????? ?????? ?????? ?????? ??????. ")
    @Test
    void ??????_?????????4() throws Exception {
        Event event = eventRepository.findBySubject("?????? ?????????").get();
        Long eventId = event.getId();

        mvc.perform(post( "/events/" + eventId + "/acceptance")
                .header("Authorization", invitedToken)
                .contentType(APPLICATION_JSON));

        mvc.perform(get("/events/" + eventId).header("Authorization", invitedToken))
                .andExpect(status().isOk());
    }

    @DisplayName("????????? ?????? No /?????? ?????? No ???????????? ???????????? ?????? ?????? ????????? ??? ?????? " +
            "403 ???????????? " +
            "{FORBIDDEN} ????????? ?????? ")
    @Test
    void ??????_?????????1() throws Exception {
        Event event = eventRepository.findBySubject("?????? ?????????").get();
        Long eventId = event.getId();

        mvc.perform(get("/events/" + eventId).header("Authorization", nonInvitedToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(BAD_ACCESS));
    }

    @DisplayName("????????? ???????????? ???????????? ????????? ????????? ???????????? " +
            "?????? ?????? 403")
    @Test
    void ??????_?????????2() throws Exception {
        Event event = eventRepository.findBySubject("?????? ?????????").get();
        Long eventId = event.getId();

        mvc.perform(get("/events/" + eventId).header("Authorization", invitedToken))
                .andExpect(status().isForbidden());
    }

    @DisplayName("???????????? ?????? ????????? ?????? ??????????????? ??????" +
            "?????? ?????? 404")
    @Test
    void ??????_?????????3() throws Exception {
        Long notExistedEventId = Long.MAX_VALUE;

        mvc.perform(get("/events/" + notExistedEventId).header("Authorization", invitedToken))
                .andExpect(status().isNotFound());
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

        MockMultipartFile file4 = new MockMultipartFile(
                "data", "", "application/json",
                "{\"nickname\" : \"??????\",\"username\": \"?????????\", \"email\": \"chamwhe@gmail.com\", \"password\": \"123456aA9\"}".getBytes());
        mvc.perform(multipart("/auth/signup")
                .file(file4));
    }
}
