package com.gnims.project.domain.schedule.service;

import com.gnims.project.domain.event.entity.Event;
import com.gnims.project.domain.event.repository.EventRepository;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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
    String hostToken = null;

    @BeforeEach
    void beforeEach() throws Exception {
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

        MvcResult result = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\",\"socialCode\": \"AUTH\" }")).andReturn();

        hostToken = result.getResponse().getHeader("Authorization");

        Long hostId = userRepository.findByNickname("딸기").get().getId();
        Long inviteeId = userRepository.findByNickname("당근").get().getId();


        //when
        mvc.perform(post("/events").header("Authorization", hostToken)
                .contentType(APPLICATION_JSON)
                .content("{\"date\": \"2023-03-15\", " +
                        "\"time\":\"16:00:00\"," +
                        "\"subject\":\"자바 스터디\"," +
                        "\"content\":\"람다, 스트림에 대해 공부합니다.\", " +
                        "\"cardColor\": \"pink\"," +
                        "\"participantsId\": " +
                        "[" + hostId + "," + inviteeId + "]}"));
    }

    @AfterEach
    void afterEach() {
        scheduleRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * 기본적으로 일정을 생성한 사람(주최자)은 일정(Schedule)에 참석(isAccepted = true)한다고 결정하였습니다.
     */
    @DisplayName("일정 상세 조회 시, " +
            "응답 결과로 초대를 수락한(isAccepted) 사람의 username 반환 " +
            "초대를 수락하지 않은 사람의 username 은 반환하지 않습니다." +
            "일정 제목, 본문, 카드 색상, 날짜, 시간, 디데이 반환")
    @Test
    void test1() throws Exception {
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);

        Event event = eventRepository.findBySubject("자바 스터디").get();
        Long eventId = event.getId();

        mvc.perform(MockMvcRequestBuilders.get("/events/" + eventId)
                .header("Authorization", hostToken)
                .contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data[?(@.subject == '%s')]","자바 스터디").exists())
                .andExpect(jsonPath("$.data[?(@.content == '%s')]","람다, 스트림에 대해 공부합니다.").exists())
                .andExpect(jsonPath("$.data[?(@.cardColor == '%s')]","pink").exists())
                .andExpect(jsonPath("$.data[?(@.date == '%s')]","2023-03-15").exists())
                .andExpect(jsonPath("$.data[?(@.time == '%s')]","16:00:00").exists())
                .andExpect(jsonPath("$..dday").isNotEmpty())
                .andExpect(jsonPath("$.data.invitees[?(@.username == '%s')]", "이땡땡").exists())
                .andExpect(jsonPath("$.data.invitees[?(@.username == '%s')]", "박땡땡").doesNotExist())
                .andExpect(jsonPath("$.data.invitees[?(@.username == '%s')]", "김땡땡").doesNotExist());
    }

    @DisplayName("일정 전체 조회 시, " +
            "응답 결과로 초대를 수락한(isAccepted) 사람의 username, profile 반환" +
            "초대를 수락하지 않은 사람의 username 은 반환하지 않습니다." +
            "일정 제목, 카드 색상, 프로필, 날짜, 시간, 디데이 반환")
    @Test
    void test2() throws Exception {
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);

        User user = userRepository.findByUsername("이땡땡").get();

        mvc.perform(MockMvcRequestBuilders.get("/users/" + user.getId() + "/events")
                        .header("Authorization", hostToken)
                        .contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data[?(@.subject == '%s')]","자바 스터디").exists())
                .andExpect(jsonPath("$.data[?(@.cardColor == '%s')]","pink").exists())
                .andExpect(jsonPath("$.data[?(@.date == '%s')]","2023-03-15").exists())
                .andExpect(jsonPath("$.data[?(@.time == '%s')]","16:00:00").exists())
                .andExpect(jsonPath("$..dday").isNotEmpty())
                .andExpect(jsonPath("$..invitees[?(@.username == '%s')]", "이땡땡").exists())
                .andExpect(jsonPath("$..invitees[?(@.profile == '%s')]", "https://gnims99.s3.ap-northeast-2.amazonaws.com/ProfilImg.png").exists())
                .andExpect(jsonPath("$..invitees[?(@.username == '%s')]", "박땡땡").doesNotExist())
                .andExpect(jsonPath("$..invitees[?(@.username == '%s')]", "김땡땡").doesNotExist());
    }

    @DisplayName("일정 전체 조회 시, 응답 결과의 data 길이는 자신이 수락(isAccepted = true)한 일정 갯수와 동일해야 한다.")
    @Test
    void test3() throws Exception {
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);

        User user = userRepository.findByNickname("딸기").get();
        Long userId = user.getId();

        mvc.perform(post("/events").header("Authorization", hostToken)
                .contentType(APPLICATION_JSON)
                .content("{\"date\": \"2023-03-15\", " +
                        "\"time\":\"16:00:00\"," +
                        "\"subject\":\"스프링 스터디\"," +
                        "\"content\":\"스프링 MVC를 공부합니다.\", " +
                        "\"cardColor\": \"black\"," +
                        "\"participantsId\": " +
                        "[" + userId + "]}"));

        mvc.perform(MockMvcRequestBuilders.get( "/users/" + user.getId() + "/events")
                        .header("Authorization", hostToken)
                        .contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data.size()").value(2));

    }

    @DisplayName("상세 조회 시 응답 값에는" +
            "이벤트를 생성한 사람(hostId)의 ID가 포함된다.")
    @Test
    void test4() throws Exception {
        //given
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);

        // 이벤트 생성자 ID
        Long hostId = userRepository.findByNickname("딸기").get().getId();

        Event event = eventRepository.findBySubject("자바 스터디").get();
        Long eventId = event.getId();

        //then
        mvc.perform(MockMvcRequestBuilders.get("/events/" + eventId)
                .header("Authorization", hostToken))
                .andExpect(jsonPath("$..hostId").value(hostId.intValue()));

    }
}
