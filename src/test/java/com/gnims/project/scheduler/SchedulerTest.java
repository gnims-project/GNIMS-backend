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

    @DisplayName("????????? ?????? ????????? ????????? ???????????? ???????????? d-day??? 1 ????????????.")
    @Test
    void test1() throws Exception {

        mvc.perform(post("/events").header("Authorization", hostToken)
                .contentType(APPLICATION_JSON)
                .content("{\"date\": \"9999-03-15\", " +
                        "\"time\":\"16:00:00\"," +
                        "\"subject\":\"?????? ?????? ??????\"," +
                        "\"content\":\"????????? ?????????????????? ?????? ??????\", " +
                        "\"participantsId\": " +
                        "[]}"));

        Event event = eventRepository.findBySubject("?????? ?????? ??????").get();
        eventScheduler.updateEventDDay();
        Event oneDayAfterEvent = eventRepository.findBySubject("?????? ?????? ??????").get();

        Assertions.assertThat(oneDayAfterEvent.getDDay()).isEqualTo(event.getDDay() - 1l);
    }

    @DisplayName("?????? ???????????? ?????? 3?????? 3??? ?????? ????????? ?????? ?????? ????????? ?????? ????????????.")
    @Test
    void test2() throws Exception {
        mvc.perform(post("/auth/password").header("Authorization", hostToken)
                .contentType(APPLICATION_JSON)
                .content("{\"email\": \"ddalgi@gmail.com\"}"));

        emailScheduler.deleteAuthMail();

        //???????????? 3?????? 3?????? ????????? ????????? ????????? ?????? ??????
        Assertions.assertThat(emailRepository.findByEmail("ddalgi@gmail.com")).isPresent();
    }

    @DisplayName("Ssemitter ????????? ??? ???????????? ????????? 0?????? ??????.")
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

    private MvcResult getMvcResult(String content) throws Exception {
        MvcResult result = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)).andReturn();
        return result;
    }
}