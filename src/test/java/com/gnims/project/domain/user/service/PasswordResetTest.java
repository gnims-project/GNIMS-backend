package com.gnims.project.domain.user.service;

import com.gnims.project.domain.user.repository.UserRepository;
import com.gnims.project.share.gmail.EmailRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import static com.gnims.project.share.message.ExceptionMessage.*;
import static com.gnims.project.share.message.ResponseMessage.SECRET_UPDATE_SUCCESS_MESSAGE;
import static com.gnims.project.share.message.ResponseMessage.SUCCESS_AUTH_EMAIL_MESSAGE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * 비밀번호 재설정 TEST
 *
 * 비밀번호 인증 로직은 API간의 연계가 되기 때문에
 * 이 테스트만 인증 순서대로
 * 단위 테스트가 아닌 기능테스트로 진행 됩니다!
 *
 * passwordUpdate의 로직 흐름은 아래와 같습니다
 * step.1 이메일 전송 -> step2. 코드 인증 -> step3. 비밀번호 재설정
 * 각 step 마다 실패,성공 케이스가 존재합니다.
 * */
@AutoConfigureMockMvc
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PasswordResetTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailRepository emailRepository;

    @BeforeAll
    void before() throws Exception {

        MockMultipartFile signupFile = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"딸기\",\"username\": \"이땡땡\", \"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}".getBytes());

        mvc.perform(multipart("/auth/signup").file(signupFile).characterEncoding("utf-8"));
    }

    @AfterAll
    void after() throws Exception {

        userRepository.deleteAll();
        emailRepository.deleteAll();
    }

    /**
     * step.1 이메일 전송
     * */
    @DisplayName("메일 날리기 성공 - 상태코드 200, db에 생성, 인증상태 false")
    @Test
    @Order(1)
    void 이메일날리기테스트() throws Exception {

        mvc.perform(post("/auth/password")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\": \"ddalgi@gmail.com\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        //DB에 저장됫는지 테스트
        Assertions.assertThat(emailRepository.findByEmail("ddalgi@gmail.com")).isPresent();

        //인증 상태 false
        Assertions.assertThat(emailRepository.findByEmail("ddalgi@gmail.com").get().getIsChecked()).isFalse();
    }

    @DisplayName("메일 날리기 실패, 등록되지 않은 이메일 - 상태코드 400, 실패 메세지 반환, db에 생성 x")
    @Test
    @Order(2)
    void 이메일날리기실패테스트() throws Exception {

        mvc.perform(post("/auth/password")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\": \"ddalg@gmail.com\"}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(NON_EXISTED_EMAIL));

        //DB에 저장됫는지 테스트
        Assertions.assertThat(emailRepository.findByEmail("ddalg@gmail.com")).isEmpty();
    }

    /**
     * step2. 코드 인증
     * */
    @DisplayName("코드 인증 성공 - 상태코드 200, 성공 메세지 반환, db에 반영, 인증상태 true")
    @Test
    @Order(5)
    void 코드인증성공테스트() throws Exception {

        String code = emailRepository.findByEmail("ddalgi@gmail.com").get().getCode();

        mvc.perform(patch("/auth/code")
                        .contentType(APPLICATION_JSON)
                        .content("{\"code\": \"" + code + "\", \"email\": \"ddalgi@gmail.com\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(SUCCESS_AUTH_EMAIL_MESSAGE));

        //인증 상태 true
        Assertions.assertThat(emailRepository.findByEmail("ddalgi@gmail.com").get().getIsChecked()).isTrue();
    }

    @DisplayName("코드 인증 실패, 이메일 틀림 - 상태코드 400, 실패 메세지 반환, db에 반영 x, 인증상태 false")
    @Test
    @Order(3)
    void 코드인증실패테스트1() throws Exception {

        String code = emailRepository.findByEmail("ddalgi@gmail.com").get().getCode();

        mvc.perform(patch("/auth/code")
                        .contentType(APPLICATION_JSON)
                        .content("{\"code\": \"" + code + "\", \"email\": \"ddalg@gmail.com\"}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(INVALID_CODE_ERROR));

        //인증 상태 true
        Assertions.assertThat(emailRepository.findByEmail("ddalgi@gmail.com").get().getIsChecked()).isFalse();
    }

    @DisplayName("코드 인증 실패, 인증 코드 유효 하지 않음 - 상태코드 400, 실패 메세지 반환, db에 반영 x, 인증상태 false")
    @Test
    @Order(4)
    void 코드인증실패테스트2() throws Exception {

        mvc.perform(patch("/auth/code")
                        .contentType(APPLICATION_JSON)
                        .content("{\"code\": \"test\", \"email\": \"ddalgi@gmail.com\"}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(INVALID_CODE_ERROR));

        //인증 상태 true
        Assertions.assertThat(emailRepository.findByEmail("ddalgi@gmail.com").get().getIsChecked()).isFalse();
    }

    /**
     * step3. 비밀번호 재설정
     * */
    @DisplayName("비밀번호 재설정 성공 - 상태코드 200, 성공 메세지 반환, db에 반영")
    @Test
    @Order(9)
    @Transactional
    void 비밀번호재설정성공테스트() throws Exception {

        //인증 상태를 true
        emailRepository.findByEmail("ddalgi@gmail.com").get().isCheckedTrue();

        mvc.perform(patch("/auth/password")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(SECRET_UPDATE_SUCCESS_MESSAGE));

        //인증 후 삭제
        Assertions.assertThat(emailRepository.findByEmail("ddalgi@gmail.com")).isEmpty();
    }

    @DisplayName("재설정 시 비밀번호 값이 null 혹은 정규식 불 일치 - 상태코드 400, 실패 메세지를 반환, db에 반영x")
    @Test
    @Order(6)
    void 비밀번호재설정실패테스트3() throws Exception {

        //비밀번호 null 값
        mvc.perform(patch("/auth/password")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\": \"ddalgi@gmail.com\"}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messages[*]").value(SECRET_EMPTY_MESSAGE));

        //비밀번호 정규식 불 일치
        mvc.perform(patch("/auth/password")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\": \"ddalgi@gmail.com\", \"password\": \"123456789\"}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messages[*]").value(SECRET_ERROR_MESSAGE));

        //DB에서 삭제 안됨
        Assertions.assertThat(emailRepository.findByEmail("ddalgi@gmail.com")).isPresent();
    }

    @DisplayName("비밀번호 재설정 실패, 이메일 틀릴 시 - 상태코드 400, 실패 메세지 반환, db에 반영x")
    @Test
    @Order(7)
    void 비밀번호재설정실패테스트1() throws Exception {

        mvc.perform(patch("/auth/password")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\": \"ddalg@gmail.com\", \"password\": \"123456aA9\"}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(INVALID_CODE_ERROR));

        //DB에서 삭제 안됨
        Assertions.assertThat(emailRepository.findByEmail("ddalgi@gmail.com")).isPresent();
    }

    @DisplayName("비밀번호 재설정 실패, 인증 상태가 false 일시 - 상태코드 400, 실패 메세지 반환, db에 반영x")
    @Test
    @Order(8)
    void 비밀번호재설정실패테스트2() throws Exception {

        //이메일 인증을 다시 날려서 인증 상태를 false로 만듬
        mvc.perform(post("/auth/password")
                .contentType(APPLICATION_JSON)
                .content("{\"email\": \"ddalgi@gmail.com\"}"));

        mvc.perform(patch("/auth/password")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\": \"ddalg@gmail.com\", \"password\": \"123456aA9\"}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(INVALID_CODE_ERROR));

        //DB에서 삭제 안됨
        Assertions.assertThat(emailRepository.findByEmail("ddalgi@gmail.com")).isPresent();
    }
}
