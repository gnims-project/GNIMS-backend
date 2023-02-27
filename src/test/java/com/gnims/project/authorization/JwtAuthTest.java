package com.gnims.project.authorization;

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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class JwtAuthTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository userRepository;

    private static String expiredToken = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiLsnpDrqr0iLCJleHAiOjE2Nzc0MTU1ODYsImlhd" +
            "CI6MTY3NzMyOTE4Nn0.BWUX8YwLleJTJ9P_TM3m4x7nGr90RsOfGmQ-_AYkMlo";

    @BeforeEach
    void beforeEach() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "data", "", "application/json",
                "{\"nickname\" : \"딸기\",\"username\": \"이땡땡\", \"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}".getBytes());
        mvc.perform(multipart("/auth/signup")
                .file(file1));
    }
    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
    }

    @DisplayName("인증 로직이 필요하지 않은 영역에서 유효하지 않은 토큰을 보내더라도 응답은 401이 아닙니다. " +
            "정상 요청의 경우 상태코드는 200 응답 " +
            "요청이 아닐 경우 상태 코드 400 응답")
    @Test
    void test1() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/auth/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"leesin5498@gmail.com\"}")
                //when 유효하지 않은 토큰을 담았을 경우
                .header("Authorization", expiredToken))
                //then 상태 코드 200
                .andExpect(status().isOk());

        mvc.perform(MockMvcRequestBuilders.post("/auth/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"ddalgi@gmail.com\"}")
                        .header("Authorization", expiredToken))
                .andExpect(status().isBadRequest());
    }
}
