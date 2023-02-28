package com.gnims.project.domain.user.service;

import com.gnims.project.domain.user.dto.SignupRequestDto;
import com.gnims.project.domain.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

@SpringBootTest
@AutoConfigureMockMvc
public class UserSignupConcurrentTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
    }

    @DisplayName("동시에 회원 가입을 요청하더라도 " +
            "단 한 개의 회원 엔티티만 DB에 저장된다.")
    @Test
    void test1() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "data", "", "application/json",
                "{\"nickname\" : \"자몽\",\"username\": \"이땡땡\", \"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}".getBytes());

        Runnable taskA = () -> {
            try {
                mvc.perform(multipart("/auth/signup").file(file1));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        Thread threadA = new Thread(taskA);
        threadA.start();

        Runnable taskB = () -> {
            try {
                mvc.perform(multipart("/auth/signup").file(file1));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        Thread threadB = new Thread(taskB);
        threadB.start();

        Thread.sleep(1000l);

        Assertions.assertThat(userRepository.findAll().size()).isEqualTo(1);
        Assertions.assertThat(userRepository.findByNickname("자몽")).isPresent();
    }
}
