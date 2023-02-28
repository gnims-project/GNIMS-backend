package com.gnims.project.domain.user.service;

import com.gnims.project.domain.user.dto.SignupRequestDto;
import com.gnims.project.domain.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
public class UserSignupConcurrentTest {

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
    void test1() throws InterruptedException {
        Runnable taskA = () -> {
            try {userService.signup(new SignupRequestDto("자몽","이재헌",
                    "leesin5498@naver.com","1234aA5678"),null);
            } catch (IOException e) {throw new RuntimeException(e);}};
        Thread threadA = new Thread(taskA);
        threadA.start();

        Runnable taskB = () -> {
            try {userService.signup(new SignupRequestDto("자몽","이재헌",
                    "leesin5498@naver.com","1234aA5678"),null);
            } catch (IOException e) {throw new RuntimeException(e);}};
        Thread threadB = new Thread(taskB);
        threadB.start();

        Thread.sleep(1000l);

        Assertions.assertThat(userRepository.findAll().size()).isEqualTo(1);
        Assertions.assertThat(userRepository.findByNickname("자몽")).isPresent();
    }
}
