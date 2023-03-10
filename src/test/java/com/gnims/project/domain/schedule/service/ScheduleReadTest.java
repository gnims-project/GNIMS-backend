package com.gnims.project.domain.schedule.service;

import com.gnims.project.domain.event.repository.EventRepository;
import com.gnims.project.domain.notification.repository.NotificationRepository;
import com.gnims.project.domain.schedule.repository.ScheduleRepository;
import com.gnims.project.domain.user.entity.User;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class ScheduleReadTest {

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
    String carrotToken = null;

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

        carrotToken = result2.getResponse().getHeader("Authorization");

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
            "?????? ????????? ????????? ?????????(isAccepted) ????????? username, profile ??????" +
            "????????? ???????????? ?????? ????????? username ??? ???????????? ????????????." +
            "?????? ??????, ?????? ??????, ?????????, ??????, ??????, ????????? ??????")
    @Test
    void test2() throws Exception {

        User user = userRepository.findByUsername("?????????").get();

        mvc.perform(get("/users/" + user.getId() + "/events")
                        .header("Authorization", hostToken)
                        .contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data[?(@.subject == '%s')]","?????? ?????????").exists())
                .andExpect(jsonPath("$.data[?(@.cardColor == '%s')]","pink").exists())
                .andExpect(jsonPath("$.data[?(@.date == '%s')]","9999-03-15").exists())
                .andExpect(jsonPath("$.data[?(@.time == '%s')]","16:00:00").exists())
                .andExpect(jsonPath("$..dday").isNotEmpty())
                .andExpect(jsonPath("$..invitees[?(@.username == '%s')]", "?????????").exists())
                .andExpect(jsonPath("$..invitees[?(@.profile == '%s')]",
                        "https://gnims99.s3.ap-northeast-2.amazonaws.com/ProfilImg.png").exists())
                .andExpect(jsonPath("$..invitees[?(@.username == '%s')]", "?????????").doesNotExist())
                .andExpect(jsonPath("$..invitees[?(@.username == '%s')]", "?????????").doesNotExist());
    }

    @DisplayName("?????? ?????? ?????? ??? " +
            "?????? ????????? data ????????? ????????? ??????(isAccepted = true)??? ?????? ????????? ???????????? ??????.")
    @Test
    void test3() throws Exception {
        User user = userRepository.findByNickname("??????").get();
        Long userId = user.getId();

        createSchedule();

        mvc.perform(get( "/users/" + userId + "/events")
                        .header("Authorization", hostToken)
                        .contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data.size()").value(2));

    }

    @DisplayName("????????? ?????? ??? ?????? ????????? " +
            "??? ????????? ?????? ???????????? totalPage??? ???????????? " +
            "??? ??????????????? size ?????? ???????????? " +
            "????????? ??????????????? ?????? schedule ????????? ???????????????.")
    @Test
    void test5() throws Exception {

        //given : ????????? 7??? ??????, beforeEach?????? ??????????????? ????????? 1??? ??? ????????? ?????? 8???
        Long hostId = userRepository.findByNickname("??????").get().getId();
        for (int i = 1; i <= 7; i++) {
            createSchedule();
        }
        //when
        mvc.perform(get("/v2/users/" + hostId + "/events").header("Authorization", hostToken)
                .param("page","0")
                .param("size","5"))
                //then ??? ????????? ??? ??????
                .andExpect(jsonPath("$.totalPage").value(2))
                //then : ?????? ??? ????????? 5??? ????????? ??????
                .andExpect(jsonPath("$.data.size()").value(5));

        mvc.perform(get("/v2/users/" + hostId + "/events").header("Authorization", hostToken)
                .param("page","1")
                .param("size","5"))
                // then : ?????? ??? ?????????(????????? ?????????) 5??? ????????? ??????
                .andExpect(jsonPath("$.data.size()").value(3));
    }

    @DisplayName("????????? ????????? ?????? ??? ???????????? ?????? ?????? ????????? ???????????? " +
            "???????????? 403 ?????? " +
            "????????? ???????????? 200 ?????? ??????.")
    @Test
    void test8() throws Exception {
        User user = userRepository.findByNickname("??????").get();
        Long userId = user.getId();

        createSchedule();
        // follow ????????? ?????? ??? ?????? ?????? ??????
        mvc.perform(get( "/users/" + userId + "/events")
                        .header("Authorization", carrotToken).contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
        // follow ?????? ??????
        mvc.perform(post("/friendship/followings/" + userId).header("Authorization", carrotToken));
        // follow ????????? ????????? ??? ??????
        mvc.perform(get( "/users/" + userId + "/events")
                        .header("Authorization", carrotToken).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()").value(2));

    }

    @DisplayName("????????? ?????? ??? ?????? ????????? String ????????? sortedBy Param?????? ?????? ????????? ????????? ??????. " +
            "sortedBy ?????? ?????? event.dDay ??? ?????? d-day??? ???????????? ?????? ???????????? ???????????? " +
            "event.createAt ??? ?????? ????????? ??????????????? ???????????? ?????? ???????????? ????????????." +
            "??? ??? param ?????? ??????????????? event.dDay ????????? ????????????.")
    @Test
    void ?????????_??????_?????????() throws Exception {
        //given : ????????? 7??? ??????, beforeEach?????? ??????????????? ????????? 1??? ??? ????????? ?????? 8???
        Long hostId = userRepository.findByNickname("??????").get().getId();
        createSchedule("\"9999-04-15\"", "\"??? ?????? ?????????\"", carrotToken);
        createSchedule("\"9999-01-15\"", "\"??? ?????? ?????????\"", carrotToken);
        createSchedule("\"9999-02-17\"", "\"??? ?????? ?????????\"", carrotToken);
        createSchedule("\"9999-03-20\"", "\"??? ?????? ?????????\"", carrotToken);

        //when
        mvc.perform(get("/v2/users/" + hostId + "/events").header("Authorization", carrotToken)
                        .param("page","0")
                        .param("size","10")) //then sortedBy : ????????? ?????? d-day??? ???????????? ?????? ???????????? ??????
                .andExpect(jsonPath("$.data[0].subject").value("??? ?????? ?????????"))
                .andExpect(jsonPath("$.data[1].subject").value("??? ?????? ?????????"));

        mvc.perform(get("/v2/users/" + hostId + "/events").header("Authorization", carrotToken)
                        .param("page","0")
                        .param("size","10")
                        .param("sortedBy", "event.dDay")) //then sortedBy : event.dDay d-day??? ???????????? ?????? ???????????? ??????
                .andExpect(jsonPath("$.data[0].subject").value("??? ?????? ?????????"))
                .andExpect(jsonPath("$.data[1].subject").value("??? ?????? ?????????"));

        mvc.perform(get("/v2/users/" + hostId + "/events").header("Authorization", carrotToken)
                        .param("page","0")
                        .param("size","10")
                        .param("sortedBy","event.createAt")) //then sortedBy : ????????? ??????????????? ???????????? ?????? ???????????? ??????
                .andExpect(jsonPath("$.data[0].subject").value("??? ?????? ?????????"));
    }

    @DisplayName("????????? - ?????? ?????? ?????? ???, ???????????? ?????? ????????? ????????? ??????????????? ?????? 403 ??????????????? ????????????.")
    @Test
    void ?????????_??????_??????_?????????1() throws Exception {
        Long userId = userRepository.findByNickname("??????").get().getId();
        createSchedule("\"9999-04-15\"", "\"??? ?????? ?????????\"", carrotToken);
        createSchedule("\"9999-01-15\"", "\"??? ?????? ?????????\"", carrotToken);
        createSchedule("\"9999-02-17\"", "\"??? ?????? ?????????\"", carrotToken);
        createSchedule("\"9999-03-20\"", "\"??? ?????? ?????????\"", carrotToken);

        //when
        mvc.perform(get("/v2/users/" + userId + "/events").header("Authorization", hostToken)
                .param("page","0")
                .param("size","10"))
                .andExpect(status().isForbidden());
    }

    @DisplayName("????????? - ?????? ?????? ?????? ???, ??????????????? ?????? ????????????.")
    @Test
    void ?????????_??????_??????_?????????2() throws Exception {
        Long userId = userRepository.findByNickname("??????").get().getId();
        createSchedule("\"9999-04-15\"", "\"??? ?????? ?????????\"", carrotToken);
        createSchedule("\"9999-01-15\"", "\"??? ?????? ?????????\"", carrotToken);
        createSchedule("\"9999-02-17\"", "\"??? ?????? ?????????\"", carrotToken);
        createSchedule("\"9999-03-20\"", "\"??? ?????? ?????????\"", carrotToken);

        // ????????? ??????
        mvc.perform(post("/friendship/followings/" + userId).header("Authorization", hostToken));

        //when
        mvc.perform(get("/v2/users/" + userId + "/events").header("Authorization", hostToken)
                        .param("page","0")
                        .param("size","10"))
                .andExpect(status().isOk());
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

    private void createSchedule() throws Exception {
        mvc.perform(post("/events").header("Authorization", hostToken)
                .contentType(APPLICATION_JSON)
                .content("{\"date\": \"9999-03-15\", " +
                        "\"time\":\"16:00:00\"," +
                        "\"subject\":\"?????? ?????????\"," +
                        "\"content\":\"??????, ???????????? ?????? ???????????????.\", " +
                        "\"cardColor\": \"pink\"," +
                        "\"participantsId\": " +
                        "[]}"));
    }

    private void createSchedule(String date, String subject, String token) throws Exception {
        mvc.perform(post("/events").header("Authorization", token)
                .contentType(APPLICATION_JSON)
                .content("{\"date\":" + date + ", " +
                        "\"time\":\"16:00:00\"," +
                        "\"subject\":" + subject + "," +
                        "\"content\":\"??????, ???????????? ?????? ???????????????.\", " +
                        "\"cardColor\": \"pink\"," +
                        "\"participantsId\": " +
                        "[]}"));
    }
}
