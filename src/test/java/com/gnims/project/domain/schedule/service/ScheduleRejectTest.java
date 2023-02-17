package com.gnims.project.domain.schedule.service;

import com.gnims.project.domain.event.entity.Event;
import com.gnims.project.domain.event.repository.EventRepository;
import com.gnims.project.domain.schedule.entity.Schedule;
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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import static com.gnims.project.domain.schedule.entity.ScheduleStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
public class ScheduleRejectTest {

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
    String inviteeToken = null;

    @BeforeEach
    void beforeEach() throws Exception {
        makeUser();

        MvcResult result = getLoginResult("{\"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}");
        hostToken = result.getResponse().getHeader("Authorization");

        MvcResult result2 = getLoginResult("{\"email\": \"danguen@gmail.com\", \"password\": \"123456aA9\"}");
        inviteeToken = result2.getResponse().getHeader("Authorization");

        Long hostId = userRepository.findByNickname("딸기").get().getId();
        Long inviteeId = userRepository.findByNickname("당근").get().getId();

        // 일정 생성
        mvc.perform(post("/events").header("Authorization", hostToken)
                .contentType(APPLICATION_JSON)
                .content("{\"date\": \"2023-03-15\", " +
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
    @DisplayName("삭제된 일정을 거절하려는 경우 " +
            "400 상태 코드와 응답 메시지 반환 " +
            "scheduleStatus는 기존(PENDING)과 동일하다.")
    @Test
    void 일정_수락_실패_케이스1() throws Exception {
        Event event = eventRepository.findBySubject("과일 정기 모임").get();
        event.removeEvent();
        Long eventId = event.getId();

        Long inviteeId = userRepository.findByNickname("당근").get().getId();

        //when
        mvc.perform(post( "/events/" + eventId + "/rejection")
                        .header("Authorization", inviteeToken)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        Schedule schedule = scheduleRepository.findByUser_IdAndEvent_Id(inviteeId, eventId).get();
        Assertions.assertThat(schedule.getScheduleStatus()).isEqualTo(PENDING);
    }

    @DisplayName("이미 수락 혹은 거절된을 거절하려는 경우 " +
            "400 상태 코드와 응답 메시지가 반환된다.")
    @Test
    void 일정_수락_실패_케이스2() throws Exception {
        Event event = eventRepository.findBySubject("과일 정기 모임").get();
        event.removeEvent();
        Long eventId = event.getId();

        Long inviteeId = userRepository.findByNickname("당근").get().getId();

        //when
        mvc.perform(post( "/events/" + eventId + "/rejection")
                        .header("Authorization", inviteeToken)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        Schedule schedule = scheduleRepository.findByUser_IdAndEvent_Id(inviteeId, eventId).get();
        Assertions.assertThat(schedule.getScheduleStatus()).isEqualTo(PENDING);
    }

    /**
     * 로직 개선 필요.
     */
    @DisplayName("초대 받지 않은 스케줄을 거절하는 경우 " +
            "500에러 발생")
    @Test
    void 일정_거절_실패_케이스3() throws Exception {

    }

    @DisplayName("이미 기한이 지난 일정을 거절하려는 경우 " +
            "???")
    @Test
    void 일정_거절_실패_케이스4() throws Exception {

    }

    @DisplayName("내가 만든 일정을 거절하려는 경우 " +
            "상태 코드 400 발생" +
            "ScheduleStatus 필드는 ACCEPT로 유지된다.")
    @Test
    void 일정_거절_실패_케이스5() throws Exception {
        Long hostId = userRepository.findByNickname("딸기").get().getId();

        Long eventId = eventRepository.findBySubject("과일 정기 모임").get().getId();
        Schedule initialSchedule = scheduleRepository.findByUser_IdAndEvent_Id(hostId, eventId).get();
        // 스케줄을 수락 전 ScheduleStatus 필드
        Assertions.assertThat(initialSchedule.getScheduleStatus()).isEqualTo(ACCEPT);
        //when
        mvc.perform(post( "/events/" + eventId + "/rejection")
                .header("Authorization", hostToken)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        Schedule acceptedSchedule = scheduleRepository.findByUser_IdAndEvent_Id(hostId, eventId).get();
        Assertions.assertThat(acceptedSchedule.getScheduleStatus()).isEqualTo(ACCEPT);
    }

    @DisplayName("일정에 초대받은 사용자가 일정 거절 시, 거절한 사용자의 Schedule 엔티티의" +
            "ScheduleStatus 필드 PENDING -> REJECT 로 변경되고" +
            "다른 사용자의 ScheduleStatus 필드에는 영향을 미치지 않는다.")
    @Test
    void 일정_거절_성공_케이스() throws Exception {
        //given
        Long inviteeId = userRepository.findByNickname("당근").get().getId();

        Long eventId = eventRepository.findBySubject("과일 정기 모임").get().getId();
        Schedule initialSchedule = scheduleRepository.findByUser_IdAndEvent_Id(inviteeId, eventId).get();
        // 스케줄을 수락 전 ScheduleStatus 필드
        Assertions.assertThat(initialSchedule.getScheduleStatus()).isEqualTo(PENDING);

        //when
        mvc.perform(post( "/events/" + eventId + "/rejection")
                .header("Authorization", inviteeToken)
                .contentType(APPLICATION_JSON));

        //then
        // 스케줄을 수락 후 ScheduleStatus 필드
        Schedule acceptedSchedule = scheduleRepository.findByUser_IdAndEvent_Id(inviteeId, eventId).get();
        Assertions.assertThat(acceptedSchedule.getScheduleStatus()).isEqualTo(REJECT);
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
