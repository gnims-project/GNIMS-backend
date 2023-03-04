    package com.gnims.project.scheduler;

import com.gnims.project.domain.event.entity.Event;
import com.gnims.project.domain.event.repository.EventRepository;
import com.gnims.project.domain.notification.repository.SseEmitterManager;
import com.gnims.project.domain.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class SchedulerTest {

    @Autowired
    EventScheduler eventScheduler;
    @Autowired
    NotificationScheduler notificationScheduler;
    @Autowired
    MockMvc mvc;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    SseEmitterManager sseEmitterManager;
    @Autowired
    UserRepository userRepository;

    String hostToken = null;

    @BeforeEach
    void beforeEach() throws Exception {
        makeUser();

        MvcResult result1 = getMvcResult("{\"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\",\"socialCode\": \"AUTH\" }");
        hostToken = result1.getResponse().getHeader("Authorization");
        }

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();

    }

    @DisplayName("등록된 모든 일정은 디데이 업데이트 진행되면 d-day가 1 감소한다.")
    @Test
    void test1() throws Exception {

        mvc.perform(post("/events").header("Authorization", hostToken)
                .contentType(APPLICATION_JSON)
                .content("{\"date\": \"9999-03-15\", " +
                        "\"time\":\"16:00:00\"," +
                        "\"subject\":\"과일 정기 모임\"," +
                        "\"content\":\"방이동 채소가게에서 저녁 식사\", " +
                        "\"participantsId\": " +
                        "[]}"));

        Event event = eventRepository.findBySubject("과일 정기 모임").get();
        eventScheduler.updateEventDDay();
        Event oneDayAfterEvent = eventRepository.findBySubject("과일 정기 모임").get();

        Assertions.assertThat(oneDayAfterEvent.getDDay()).isEqualTo(event.getDDay() - 1l);
    }

    @DisplayName("Ssemitter 초기화 시 이미터의 갯수는 0개가 된다.")
    @Test
    void test2() {
        sseEmitterManager.save(1l);
        int SseEmittersSize = sseEmitterManager.getSseEmitters().size();
        Assertions.assertThat(SseEmittersSize).isGreaterThan(0);

        notificationScheduler.clearEmitterContainer();
        int updateSseEmittersSize = sseEmitterManager.getSseEmitters().size();
        Assertions.assertThat(updateSseEmittersSize).isEqualTo(0);
    }

    @DisplayName("이메일 테스트 진행")
    @Disabled("민우님 화이팅!")
    @Test
    void test3() {
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

    private MvcResult getMvcResult(String content) throws Exception {
        MvcResult result = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)).andReturn();
        return result;
    }
}