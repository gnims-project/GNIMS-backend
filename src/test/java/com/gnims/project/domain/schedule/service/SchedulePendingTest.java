package com.gnims.project.domain.schedule.service;

import com.gnims.project.domain.event.entity.Event;
import com.gnims.project.domain.event.repository.EventRepository;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
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

    private void makeSchedule(Long hostId, Long inviteeId) throws Exception {
        mvc.perform(post("/events").header("Authorization", hostToken)
                .contentType(APPLICATION_JSON)
                .content("{\"date\": \"9999-03-15\", " +
                        "\"time\":\"12:00:00\"," +
                        "\"subject\":\"과일 정기 모임\"," +
                        "\"content\":\"채소가게에서 저녁 식사\", " +
                        "\"participantsId\": " +
                        "[" + hostId + "," + inviteeId + "]}"));
    }

    @AfterEach
    void afterEach() {
        scheduleRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("수락/거절 대기중인 일정 조회 시 " +
            "대기상태인 일정 만큼 data가 반환되고 " +
            "반환 값에는 hostname : {일정에 초대한 사람} 을 포함된다.")
    @Test
    void 대기중인_일정_조회_조건1() throws Exception {
        //given
        Long hostId = userRepository.findByNickname("딸기").get().getId();
        Long inviteeId = userRepository.findByNickname("당근").get().getId();
        makeSchedule(hostId, inviteeId);

        mvc.perform(get("/v2/events/pending").header("Authorization", inviteeToken))
                .andExpect(jsonPath("$.data.size()").value(1))
                .andExpect(jsonPath("$..hostname").value("이땡땡"));

    }

    @DisplayName("수락/거절 대기중인 일정 조회 시 " +
            "이미 수락/거절된 일정(scheduleStatus ACCEPT or REJECT)은 조회되지 않는다.")
    @Test
    void 대기중인_일정_조회_조건2() throws Exception {

        //이미 수락된 일정 given
        Long hostId = userRepository.findByNickname("딸기").get().getId();
        Long inviteeId = userRepository.findByNickname("당근").get().getId();
        makeSchedule(hostId, inviteeId);

        //then - 호스트는 자동 스케줄 자동 수락(ACCEPT) 상태
        mvc.perform(get("/v2/events/pending").header("Authorization", hostToken))
                .andExpect(jsonPath("$.data.size()").value(0));

        //이미 거절된 일정 given
        Event event = eventRepository.findBySubject("과일 정기 모임").get();
        mvc.perform(post("/events/"+event.getId() + "/rejection").header("Authorization", inviteeToken));

        //then - 호스트는 자동 스케줄 자동 수락(ACCEPT) 상태
        mvc.perform(get("/v2/events/pending").header("Authorization", inviteeToken))
                .andExpect(jsonPath("$.data.size()").value(0));

    }

    @DisplayName("수락/거절 대기중인 일정 조회 시 " +
            "이미 기한이 지난 일정은 조회되지 않는다.")
    @Test
    void 대기중인_일정_조회_조건3() throws Exception {
        //given
        Long hostId = userRepository.findByNickname("딸기").get().getId();
        Long inviteeId = userRepository.findByNickname("당근").get().getId();

        //과거 일정 생성
        mvc.perform(post("/events").header("Authorization", hostToken)
                .contentType(APPLICATION_JSON)
                .content("{\"date\": \"2000-03-15\", " +
                        "\"time\":\"12:00:00\"," +
                        "\"subject\":\"과일 정기 모임\"," +
                        "\"content\":\"채소가게에서 저녁 식사\", " +
                        "\"participantsId\": " +
                        "[" + hostId + "," + inviteeId + "]}"));

        mvc.perform(get("/v2/events/pending").header("Authorization", inviteeToken))
                .andExpect(jsonPath("$.data.size()").value(0));
    }

    @DisplayName("수락/거절 대기중인 일정 조회 시 " +
            "자신이 초대받은 일정만 조회된다.")
    @Test
    void 대기중인_일정_조회_조건4() throws Exception {
        //given - 일정을 추가합니다.
        Long hostId = userRepository.findByNickname("딸기").get().getId();
        Long inviteeId = userRepository.findByNickname("당근").get().getId();
        makeSchedule(hostId, inviteeId);

        MvcResult result = getLoginResult("{\"email\": \"suback@gmail.com\", \"password\": \"123456aA9\"}");
        String uninvitedToken = result.getResponse().getHeader("Authorization");

        // then - 초대 받지 않은 사람
        mvc.perform(get("/v2/events/pending").header("Authorization", uninvitedToken))
                .andExpect(jsonPath("$.data.size()").value(0));

        // then - 초대 받은 사람
        mvc.perform(get("/v2/events/pending").header("Authorization", inviteeToken))
                .andExpect(jsonPath("$.data.size()").value(1));
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

    private MvcResult getLoginResult(String content) throws Exception {
        MvcResult result2 = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)).andReturn();
        return result2;
    }
}
