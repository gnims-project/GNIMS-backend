package com.gnims.project.domain.schedule.service;

import com.gnims.project.domain.event.entity.Event;
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

        Long hostId = userRepository.findByNickname("딸기").get().getId();
        Long inviteeId = userRepository.findByNickname("당근").get().getId();


        //when
        mvc.perform(post("/events").header("Authorization", hostToken)
                .contentType(APPLICATION_JSON)
                .content("{\"date\": \"9999-03-15\", " +
                        "\"time\":\"16:00:00\"," +
                        "\"subject\":\"자바 스터디\"," +
                        "\"content\":\"람다, 스트림에 대해 공부합니다.\", " +
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
     * 기본적으로 일정을 생성한 사람(주최자)은 일정(Schedule)에 참석(isAccepted = true)한다고 결정하였습니다.
     */

    @DisplayName("일정 전체 조회 시, " +
            "응답 결과로 초대를 수락한(isAccepted) 사람의 username, profile 반환" +
            "초대를 수락하지 않은 사람의 username 은 반환하지 않습니다." +
            "일정 제목, 카드 색상, 프로필, 날짜, 시간, 디데이 반환")
    @Test
    void test2() throws Exception {

        User user = userRepository.findByUsername("이땡땡").get();

        mvc.perform(get("/users/" + user.getId() + "/events")
                        .header("Authorization", hostToken)
                        .contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data[?(@.subject == '%s')]","자바 스터디").exists())
                .andExpect(jsonPath("$.data[?(@.cardColor == '%s')]","pink").exists())
                .andExpect(jsonPath("$.data[?(@.date == '%s')]","9999-03-15").exists())
                .andExpect(jsonPath("$.data[?(@.time == '%s')]","16:00:00").exists())
                .andExpect(jsonPath("$..dday").isNotEmpty())
                .andExpect(jsonPath("$..invitees[?(@.username == '%s')]", "이땡땡").exists())
                .andExpect(jsonPath("$..invitees[?(@.profile == '%s')]",
                        "https://gnims99.s3.ap-northeast-2.amazonaws.com/ProfilImg.png").exists())
                .andExpect(jsonPath("$..invitees[?(@.username == '%s')]", "박땡땡").doesNotExist())
                .andExpect(jsonPath("$..invitees[?(@.username == '%s')]", "김땡땡").doesNotExist());
    }

    @DisplayName("일정 전체 조회 시 " +
            "응답 결과의 data 길이는 자신이 수락(isAccepted = true)한 일정 갯수와 동일해야 한다.")
    @Test
    void test3() throws Exception {
        User user = userRepository.findByNickname("딸기").get();
        Long userId = user.getId();

        createSchedule();

        mvc.perform(get( "/users/" + userId + "/events")
                        .header("Authorization", hostToken)
                        .contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data.size()").value(2));

    }

    @DisplayName("페이징 조회 시 응답 값에는 " +
            "총 페이지 수를 의미하는 totalPage를 포함하고 " +
            "한 페이지에는 size 만큼 조회되고 " +
            "마지막 페이지에는 남은 schedule 만큼만 조회됩니다.")
    @Test
    void test5() throws Exception {

        //given : 스케줄 7번 생성, beforeEach에서 만들어지는 스케줄 1번 총 스케줄 갯수 8개
        Long hostId = userRepository.findByNickname("딸기").get().getId();
        for (int i = 1; i <= 7; i++) {
            createSchedule();
        }
        //when
        mvc.perform(get("/v2-page/users/" + hostId + "/events").header("Authorization", hostToken)
                .param("page","0")
                .param("size","5"))
                //then 총 페이지 수 반환
                .andExpect(jsonPath("$.totalPage").value(2))
                //then : 첫번 째 페이지 5개 스케줄 반환
                .andExpect(jsonPath("$.data.size()").value(5));

        mvc.perform(get("/v2-page/users/" + hostId + "/events").header("Authorization", hostToken)
                .param("page","1")
                .param("size","5"))
                // then : 두번 째 페이지(마지막 페이지) 5개 스케줄 반환
                .andExpect(jsonPath("$.data.size()").value(3));
    }

    @DisplayName("페이징 조회 시 정렬 기준은 String 타입의 sortedBy Param으로 받고 결과는 다음과 같다. " +
            "sortedBy 생략 혹은 event.dDay 일 경우 d-day를 기준으로 오름 차순으로 정렬되고 " +
            "event.createAt 일 때는 이벤트 생성일자를 기준으로 내림 차순으로 정렬된다." +
            "그 외 param 값이 들어온다면 400 에러를 던진다.")
    @Test
    void test6() throws Exception {
        //given : 스케줄 7번 생성, beforeEach에서 만들어지는 스케줄 1번 총 스케줄 갯수 8개
        Long hostId = userRepository.findByNickname("당근").get().getId();
        createSchedule("\"9999-04-15\"", "\"첫 번째 스케줄\"", carrotToken);
        createSchedule("\"9999-01-15\"", "\"두 번째 스케줄\"", carrotToken);
        createSchedule("\"9999-02-17\"", "\"세 번째 스케줄\"", carrotToken);
        createSchedule("\"9999-03-20\"", "\"네 번째 스케줄\"", carrotToken);

        //when
        mvc.perform(get("/v2-page/users/" + hostId + "/events").header("Authorization", carrotToken)
                        .param("page","0")
                        .param("size","10"))
                //then sortedBy : 생략될 경우 d-day를 기준으로 오름 차순으로 정렬
                .andExpect(jsonPath("$.data[0].subject").value("두 번째 스케줄"))
                .andExpect(jsonPath("$.data[1].subject").value("세 번째 스케줄"));

        mvc.perform(get("/v2-page/users/" + hostId + "/events").header("Authorization", carrotToken)
                        .param("page","0")
                        .param("size","10")
                        .param("sortedBy", "event.dDay"))
                //then sortedBy : event.dDay d-day를 기준으로 오름 차순으로 정렬
                .andExpect(jsonPath("$.data[0].subject").value("두 번째 스케줄"))
                .andExpect(jsonPath("$.data[1].subject").value("세 번째 스케줄"));

        mvc.perform(get("/v2-page/users/" + hostId + "/events").header("Authorization", carrotToken)
                        .param("page","0")
                        .param("size","10")
                        .param("sortedBy","event.createAt"))
                //then sortedBy : 이벤트 생성일자를 기준으로 내림 차순으로 정렬
                .andExpect(jsonPath("$.data[0].subject").value("네 번째 스케줄"));

        mvc.perform(get("/v2-page/users/" + hostId + "/events").header("Authorization", carrotToken)
                        .param("page","0")
                        .param("size","10")
                        .param("sortedBy","event.modifiedAt"))
                // then : sortedBy param 에 지정되지 않은 정렬 기준이 들어갔을 때
                .andExpect(status().isBadRequest());
    }

    @DisplayName("팔로우 관계가 아닐 때 상대방의 전체 일정 조회를 시도하면 " +
            "상태코드 403 응답 " +
            "팔로우 관계라면 200 응답 한다.")
    @Test
    void test8() throws Exception {
        User user = userRepository.findByNickname("딸기").get();
        Long userId = user.getId();

        createSchedule();

        // follow 관계가 아닐 때 전체 일정 조회
        mvc.perform(get( "/users/" + userId + "/events")
                        .header("Authorization", carrotToken)
                        .contentType(APPLICATION_JSON))
                        .andExpect(status().isForbidden());

        // follow 관계 맺기
        mvc.perform(post("/friendship/followings/" + userId)
                .header("Authorization", carrotToken));


        // follow 관계를 맺고난 뒤 조회
        mvc.perform(get( "/users/" + userId + "/events")
                        .header("Authorization", carrotToken)
                        .contentType(APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.size()").value(2));

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

        MockMultipartFile file4 = new MockMultipartFile(
                "data", "", "application/json",
                "{\"nickname\" : \"참외\",\"username\": \"최땡땡\", \"email\": \"chamwhe@gmail.com\", \"password\": \"123456aA9\"}".getBytes());
        mvc.perform(multipart("/auth/signup")
                .file(file4));
    }

    private void createSchedule() throws Exception {
        mvc.perform(post("/events").header("Authorization", hostToken)
                .contentType(APPLICATION_JSON)
                .content("{\"date\": \"9999-03-15\", " +
                        "\"time\":\"16:00:00\"," +
                        "\"subject\":\"자바 스터디\"," +
                        "\"content\":\"람다, 스트림에 대해 공부합니다.\", " +
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
                        "\"content\":\"람다, 스트림에 대해 공부합니다.\", " +
                        "\"cardColor\": \"pink\"," +
                        "\"participantsId\": " +
                        "[]}"));
    }
}
