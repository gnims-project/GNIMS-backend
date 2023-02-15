package com.gnims.project.domain.user.service;

import com.gnims.project.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    PlatformTransactionManager transactionManager;

    TransactionStatus status = null;

    @BeforeEach
    void beforeEach() throws Exception {

        MockMultipartFile signupFile1 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"딸기\",\"username\": \"이땡땡\", \"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}".getBytes());

        mvc.perform(multipart("/auth/signup").file(signupFile1).characterEncoding("utf-8"));
    }

    @DisplayName("로그인 성공 - 상태코드 200, 헤더에 토큰 반환")
    @Test
    void 로그인성공테스트() throws Exception {
        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andExpect(jsonPath("$.message").value("로그인 성공"));
    }

    @DisplayName("로그인 시 등록된 이메일 없음 - 상태코드 401, 실패 메세지 반환")
    @Test
    void 로그인실패테스트1() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"ddalgi2@gmail.com\", \"password\": \"123456aA9\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("이메일 혹은 비밀번호가 일치하지 않습니다."));
    }

    @DisplayName("로그인 시 등록된 이메일 있음, 비밀번호 틀림 - 상태코드 401, 실패 메세지 반환")
    @Test
    void 로그인실패테스트2() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"ddalgi@gmail.com\", \"password\": \"123456a9\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("이메일 혹은 비밀번호가 일치하지 않습니다."));
    }
}
