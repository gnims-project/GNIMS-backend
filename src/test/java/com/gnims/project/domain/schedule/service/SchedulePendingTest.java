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
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
public class SchedulePendingTest {
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

        MvcResult result = getLoginResult("{\"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}");
        hostToken = result.getResponse().getHeader("Authorization");

        MvcResult result2 = getLoginResult("{\"email\": \"danguen@gmail.com\", \"password\": \"123456aA9\"}");
        inviteeToken = result2.getResponse().getHeader("Authorization");

    }

    private void makeSchedule(Long hostId, Long inviteeId) throws Exception {
        mvc.perform(post("/events").header("Authorization", hostToken)
                .contentType(APPLICATION_JSON)
                .content("{\"date\": \"9999-03-15\", " +
                        "\"time\":\"12:00:00\"," +
                        "\"subject\":\"?????? ?????? ??????\"," +
                        "\"content\":\"?????????????????? ?????? ??????\", " +
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

    @DisplayName("??????/?????? ???????????? ?????? ?????? ??? " +
            "??????????????? ?????? ?????? data??? ???????????? " +
            "?????? ????????? hostname : {????????? ????????? ??????} ??? ????????????.")
    @Test
    void ????????????_??????_??????_??????1() throws Exception {
        //given
        Long hostId = userRepository.findByNickname("??????").get().getId();
        Long inviteeId = userRepository.findByNickname("??????").get().getId();
        makeSchedule(hostId, inviteeId);

        mvc.perform(get("/v2/events/pending").header("Authorization", inviteeToken))
                .andExpect(jsonPath("$.data.size()").value(1))
                .andExpect(jsonPath("$..hostname").value("?????????"));

    }

    @DisplayName("??????/?????? ???????????? ?????? ?????? ??? " +
            "?????? ??????/????????? ??????(scheduleStatus ACCEPT or REJECT)??? ???????????? ?????????.")
    @Test
    void ????????????_??????_??????_??????2() throws Exception {

        //?????? ????????? ?????? given
        Long hostId = userRepository.findByNickname("??????").get().getId();
        Long inviteeId = userRepository.findByNickname("??????").get().getId();
        makeSchedule(hostId, inviteeId);

        //then - ???????????? ?????? ????????? ?????? ??????(ACCEPT) ??????
        mvc.perform(get("/v2/events/pending").header("Authorization", hostToken))
                .andExpect(jsonPath("$.data.size()").value(0));

        //?????? ????????? ?????? given
        Event event = eventRepository.findBySubject("?????? ?????? ??????").get();
        mvc.perform(post("/events/"+event.getId() + "/rejection").header("Authorization", inviteeToken));

        //then - ???????????? ?????? ????????? ?????? ??????(ACCEPT) ??????
        mvc.perform(get("/v2/events/pending").header("Authorization", inviteeToken))
                .andExpect(jsonPath("$.data.size()").value(0));

    }

    @DisplayName("??????/?????? ???????????? ?????? ?????? ??? " +
            "?????? ????????? ?????? ????????? ???????????? ?????????.")
    @Test
    void ????????????_??????_??????_??????3() throws Exception {
        //given
        Long hostId = userRepository.findByNickname("??????").get().getId();
        Long inviteeId = userRepository.findByNickname("??????").get().getId();

        //?????? ?????? ??????
        mvc.perform(post("/events").header("Authorization", hostToken)
                .contentType(APPLICATION_JSON)
                .content("{\"date\": \"2000-03-15\", " +
                        "\"time\":\"12:00:00\"," +
                        "\"subject\":\"?????? ?????? ??????\"," +
                        "\"content\":\"?????????????????? ?????? ??????\", " +
                        "\"participantsId\": " +
                        "[" + hostId + "," + inviteeId + "]}"));

        mvc.perform(get("/v2/events/pending").header("Authorization", inviteeToken))
                .andExpect(jsonPath("$.data.size()").value(0));
    }

    @DisplayName("??????/?????? ???????????? ?????? ?????? ??? " +
            "????????? ???????????? ????????? ????????????.")
    @Test
    void ????????????_??????_??????_??????4() throws Exception {
        //given - ????????? ???????????????.
        Long hostId = userRepository.findByNickname("??????").get().getId();
        Long inviteeId = userRepository.findByNickname("??????").get().getId();
        makeSchedule(hostId, inviteeId);

        MvcResult result = getLoginResult("{\"email\": \"suback@gmail.com\", \"password\": \"123456aA9\"}");
        String uninvitedToken = result.getResponse().getHeader("Authorization");

        // then - ?????? ?????? ?????? ??????
        mvc.perform(get("/v2/events/pending").header("Authorization", uninvitedToken))
                .andExpect(jsonPath("$.data.size()").value(0));

        // then - ?????? ?????? ??????
        mvc.perform(get("/v2/events/pending").header("Authorization", inviteeToken))
                .andExpect(jsonPath("$.data.size()").value(1));
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
}
