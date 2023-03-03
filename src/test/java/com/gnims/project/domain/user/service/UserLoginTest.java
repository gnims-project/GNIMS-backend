package com.gnims.project.domain.user.service;

import com.gnims.project.domain.user.repository.UserRepository;
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
import org.springframework.transaction.annotation.Transactional;

import static com.gnims.project.share.message.ExceptionMessage.MISMATCH_EMAIL_OR_SECRET;
import static com.gnims.project.share.message.ResponseMessage.LOGIN_SUCCESS_MESSAGE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 일반 로그인 TEST
 * */
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class UserLoginTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository userRepository;

    String invalidToken = "test";

    @BeforeEach
    void beforeEach() throws Exception {

        MockMultipartFile signupFile1 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"딸기\",\"username\": \"이땡땡\", \"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}".getBytes());

        mvc.perform(multipart("/auth/signup").file(signupFile1).characterEncoding("utf-8"));
    }

    @AfterEach
    void afterEach() throws Exception {

        userRepository.deleteAll();
    }

    @DisplayName("로그인 성공 - 상태코드 200, 헤더에 토큰 반환")
    @Test
    void 로그인성공테스트() throws Exception {

        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andExpect(jsonPath("$.message").value(LOGIN_SUCCESS_MESSAGE));
    }

    @DisplayName("로그인 성공, 토큰이 같이 왓을 시 - 상태코드 200, 헤더에 토큰 반환")
    @Test
    void 로그인성공테스트2() throws Exception {

        mvc.perform(post("/auth/login").header("Authorization", invalidToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andExpect(jsonPath("$.message").value(LOGIN_SUCCESS_MESSAGE));
    }

    @DisplayName("로그인 시 등록된 이메일 없음 - 상태코드 401, 실패 메세지 반환")
    @Test
    void 로그인실패테스트1() throws Exception {

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"ddalgi2@gmail.com\", \"password\": \"123456aA9\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(MISMATCH_EMAIL_OR_SECRET));
    }

    @DisplayName("로그인 시 등록된 이메일 있음, 비밀번호 틀림 - 상태코드 401, 실패 메세지 반환")
    @Test
    void 로그인실패테스트2() throws Exception {

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"ddalgi@gmail.com\", \"password\": \"123456a9\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(MISMATCH_EMAIL_OR_SECRET));
    }
}
