    package com.gnims.project.scheduler;

    import com.gnims.project.domain.event.entity.Event;
    import com.gnims.project.domain.event.repository.EventRepository;
    import com.gnims.project.domain.notification.repository.SseEmitterManager;
    import com.gnims.project.domain.user.repository.UserRepository;
    import com.gnims.project.share.email.EmailRepository;
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
    EmailScheduler emailScheduler;
    @Autowired
    NotificationScheduler notificationScheduler;
    @Autowired
    MockMvc mvc;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    EmailRepository emailRepository;
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
        emailRepository.deleteAll();
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

    @DisplayName("현재 시간으로 부터 3시간 3분 전에 생성된 인증 메일 객체를 모두 삭제한다.")
    @Test
    void test2() throws Exception {
        mvc.perform(post("/auth/password").header("Authorization", hostToken)
                .contentType(APPLICATION_JSON)
                .content("{\"email\": \"ddalgi@gmail.com\"}"));

        emailScheduler.deleteAuthMail();

        //생성되고 3시간 3분이 지나지 않았기 때문에 삭제 안됨
        Assertions.assertThat(emailRepository.findByEmail("ddalgi@gmail.com")).isPresent();
    }

    @DisplayName("Ssemitter 초기화 시 이미터의 갯수는 0개가 된다.")
    @Test
    void test3() {
        sseEmitterManager.save(1l);
        int SseEmittersSize = sseEmitterManager.getSseEmitters().size();
        Assertions.assertThat(SseEmittersSize).isGreaterThan(0);

        notificationScheduler.clearEmitterContainer();
        int updateSseEmittersSize = sseEmitterManager.getSseEmitters().size();
        Assertions.assertThat(updateSseEmittersSize).isEqualTo(0);
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