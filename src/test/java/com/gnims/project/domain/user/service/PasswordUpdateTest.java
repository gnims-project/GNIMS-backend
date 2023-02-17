package com.gnims.project.domain.user.service;

import com.gnims.project.domain.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * 비밀번호 업데이트 TEST
 * */
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class PasswordUpdateTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    TransactionStatus status = null;

    String token = null;

    @BeforeEach
    void beforeEach() throws Exception {

        MockMultipartFile signupFile1 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"딸기\",\"username\": \"이땡땡\", \"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}".getBytes());

        mvc.perform(multipart("/auth/signup").file(signupFile1).characterEncoding("utf-8"));

        MvcResult result = mvc.perform(post("/auth/login")
                .contentType(APPLICATION_JSON)
                .content("{\"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}")).andReturn();

        token = result.getResponse().getHeader("Authorization");
    }

    @DisplayName("비밀번호 업데이트 성공 - 상태코드 200, 성공 메세지를 반환, db 반영x")
    @Test
    void 업데이트성공테스트() throws Exception {

        mvc.perform(patch("/users/password").header("Authorization", token)
                .contentType(APPLICATION_JSON)
                .content("{\"oldPassword\": \"123456aA9\", " +
                        "\"newPassword\": \"12345aA789\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[?(@.message == '%s')]", "비밀번호 바꾸기 성공").exists());

        Assertions.assertThat(passwordEncoder.matches("12345aA789", userRepository.findByNickname("딸기")
                .get().getPassword())).isTrue();
    }

    @DisplayName("비밀번호 정규식 불일치 실패 - 상태코드 400, 실패 메세지를 반환, db 반영x")
    @Test
    void 업데이트실패테스트1() throws Exception {

        mvc.perform(patch("/users/password").header("Authorization", token)
                        .contentType(APPLICATION_JSON)
                        .content("{\"oldPassword\": \"123456aA9\", " +
                                "\"newPassword\": \"12345789\"}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[?(@.messages == ['%s'])]", "비밀번호는 영문/숫자를 포함하여 8~16자로 입력해야합니다.").exists());

        Assertions.assertThat(passwordEncoder.matches("12345789", userRepository.findByNickname("딸기")
                .get().getPassword())).isFalse();
    }

    @DisplayName("비밀번호 기존과 일치 실패 - 상태코드 400, 실패 메세지를 반환, db 반영x")
    @Test
    void 업데이트실패테스트2() throws Exception {

        mvc.perform(patch("/users/password").header("Authorization", token)
                        .contentType(APPLICATION_JSON)
                        .content("{\"oldPassword\": \"123456aA9\", " +
                                "\"newPassword\": \"123456aA9\"}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[?(@.message == '%s')]", "기존의 비밀번호와 같은 비밀번호 입니다!").exists());

        Assertions.assertThat(passwordEncoder.matches("123456aA9", userRepository.findByNickname("딸기")
                .get().getPassword())).isTrue();
    }

    @DisplayName("현재 비밀번호 불일치 - 상태코드 400, 실패 메세지를 반환, db 반영x")
    @Test
    void 업데이트실패테스트3() throws Exception {

        mvc.perform(patch("/users/password").header("Authorization", token)
                        .contentType(APPLICATION_JSON)
                        .content("{\"oldPassword\": \"123456aA89\", " +
                                "\"newPassword\": \"12345aA789\"}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[?(@.message == '%s')]", "현재 비밀번호가 일치하지 않습니다.").exists());

        Assertions.assertThat(passwordEncoder.matches("12345aA789", userRepository.findByNickname("딸기")
                .get().getPassword())).isFalse();
    }
}
