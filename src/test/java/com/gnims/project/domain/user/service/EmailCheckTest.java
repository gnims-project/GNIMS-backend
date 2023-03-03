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

import static com.gnims.project.share.message.ExceptionMessage.*;
import static com.gnims.project.share.message.ResponseMessage.CHECK_EMAIL_MESSAGE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 이메일 중복체크 TEST
 * */
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class EmailCheckTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void beforeEach() throws Exception {

        MockMultipartFile signupFile1 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"딸기\",\"username\": \"이땡땡\", \"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}".getBytes());

        mvc.perform(multipart("/auth/signup").file(signupFile1).characterEncoding("utf-8"));
    }

    @AfterEach
    void afterEach() throws Exception {

        userRepository.deleteAll();
    }

    @DisplayName("이메일 중복체크 성공 - 상태코드 200, 성공 메세지 반환")
    @Test
    void 중복체크성공테스트() throws Exception {

        String expression = "$.[?(@.message == '%s')]";

        mvc.perform(post("/auth/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"orange@gmail.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expression, CHECK_EMAIL_MESSAGE).exists());
    }

    @DisplayName("이메일 중복체크 null or 정규식 실패 - 상태코드 400, 실패 메세지 반환")
    @Test
    void 중복체크실패테스트1() throws Exception {

        String expression = "$.[?(@.messages == ['%s'])]";

        mvc.perform(post("/auth/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(expression, EMAIL_EMPTY_MESSAGE).exists());

        mvc.perform(post("/auth/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"orange@gmailcom\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(expression, EMAIL_ERROR_MESSAGE).exists());
    }

    @DisplayName("이메일 중복체크 중복 실패 - 상태코드 400, 실패 메세지 반환")
    @Test
    void 중복체크실패테스트2() throws Exception {

        String expression = "$.[?(@.message == '%s')]";

        mvc.perform(post("/auth/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"ddalgi@gmail.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(expression, ALREADY_REGISTERED_EMAIL).exists());
    }
}
