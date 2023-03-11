package com.gnims.project.domain.user.service;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static com.gnims.project.share.message.ExceptionMessage.*;
import static com.gnims.project.share.message.ResponseMessage.SECRET_UPDATE_SUCCESS_MESSAGE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    String token = null;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void beforeEach() throws Exception {
        MockMultipartFile signupFile1 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"딸기\",\"username\": \"이땡땡\", \"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}".getBytes());

        mvc.perform(multipart("/auth/signup").file(signupFile1).characterEncoding("utf-8"));

        MvcResult result = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}")).andReturn();

        token = result.getResponse().getHeader("Authorization");
    }

    @AfterEach
    void afterEach(){
        userRepository.deleteAll();
    }

    @DisplayName("비밀번호 업데이트 성공 - 상태코드 200, 성공 메세지를 반환, db에 반영")
    @Test
    void 비밀번호업데이트성공테스트() throws Exception {
        mvc.perform(patch("/users/password").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\": \"123456aA9\",\"newPassword\": \"1234aA5678\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SECRET_UPDATE_SUCCESS_MESSAGE));

        Assertions.assertThat(passwordEncoder.matches("1234aA5678", userRepository.findByNickname("딸기").get().getPassword())).isTrue();
    }

    @DisplayName("비밀번호 업데이트 실패, old/new 비밀번호가 같음 - 상태코드 400, 실패 메세지를 반환, db에 변화 x")
    @Test
    void 비밀번호업데이트실패테스트1() throws Exception {
        mvc.perform(patch("/users/password").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\": \"123456aA9\",\"newPassword\": \"123456aA9\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(THE_SAME_SECRET_AS_BEFORE));

        Assertions.assertThat(passwordEncoder.matches("123456aA9", userRepository.findByNickname("딸기").get().getPassword())).isTrue();
    }

    @DisplayName("비밀번호 업데이트 실패, old 비밀번호 불 일치 - 상태코드 400, 실패 메세지를 반환, db에 반영 x")
    @Test
    void 비밀번호업데이트실패테스트2() throws Exception {
        mvc.perform(patch("/users/password").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\": \"1234aA567\",\"newPassword\": \"1234aA5678\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(CURRENT_MISMATCHED_SECRET));

        Assertions.assertThat(passwordEncoder.matches("1234aA5678", userRepository.findByNickname("딸기").get().getPassword())).isFalse();
    }

    @DisplayName("비밀번호 업데이트 실패, old/new 비밀번호 null 혹은 정규식 불 일치 - 상태코드 400, 실패 메세지를 반환, db에 반영 x")
    @Test
    void 비밀번호업데이트실패테스트3() throws Exception {
        //비밀번호 null 값
        mvc.perform(patch("/users/password").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\": \"1234aA567\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[*]").value(NEW_SECRET_EMPTY_MESSAGE));

        //DB 반영 x
        Assertions.assertThat(passwordEncoder.matches("1234aA567", userRepository.findByNickname("딸기").get().getPassword())).isFalse();

        //비밀번호 정규식 불 일치
        mvc.perform(patch("/users/password").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\": \"1234aA567\",\"newPassword\": \"12345678\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[*]").value(SECRET_ERROR_MESSAGE));

        //DB 반영 x
        Assertions.assertThat(passwordEncoder.matches("12345678", userRepository.findByNickname("딸기").get().getPassword())).isFalse();
    }
}
