package com.gnims.project.domain.friendship.service;

import com.gnims.project.domain.friendship.repository.FriendshipRepository;
import com.gnims.project.domain.user.entity.User;
import com.gnims.project.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * 팔로워 조회 TEST
 */
@AutoConfigureMockMvc
@SpringBootTest
public class FollowerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FriendshipRepository friendshipRepository;

    String ddalgiToken = null;
    String subackToken = null;

    @Autowired
    PlatformTransactionManager transactionManager;

    TransactionStatus status = null;

    @BeforeEach
    void beforeEach() throws Exception {
        mvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\" : \"딸기\", \"email\": \"ddalgi@gmail.com\", \"password\": \"123456Aa9\"}"));

        mvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\" : \"당근\", \"email\": \"danguen@gmail.com\", \"password\": \"123456Aa9\"}"));

        mvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\" : \"수박\", \"email\": \"suback@gmail.com\", \"password\": \"123456Aa9\"}"));

        mvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\" : \"참외\", \"email\": \"chamwhe@gmail.com\", \"password\": \"123456Aa9\"}"));

        MvcResult result = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"ddalgi@gmail.com\", \"password\": \"123456Aa9\"}")).andReturn();

        ddalgiToken = result.getResponse().getHeader("Authorization");

        MvcResult result2 = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"suback@gmail.com\", \"password\": \"123456Aa9\"}")).andReturn();

        subackToken = result2.getResponse().getHeader("Authorization");
    }

    @AfterEach
    void afterEach() {
        friendshipRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("상대방이 나를 팔로우할 시 - 상태코드 200, 내 팔로워 목록에 {상대방 닉네임} 반환")
    @Test
    void test1() throws Exception {
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);
        //given
        String expression = "$.data[?(@.nickname == '%s')]";

        User ddalgi = userRepository.findByUsername("딸기").get();
        Long ddalgiId = ddalgi.getId();
        mvc.perform(MockMvcRequestBuilders.post("/friendship/followings/" + ddalgiId)
                .header("Authorization", subackToken));

        //when
        mvc.perform(MockMvcRequestBuilders.get("/friendship/followers")
                .header("Authorization", ddalgiToken))

                //then
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath(expression,"수박").exists());
    }

    @DisplayName("여러명이 나를 팔로우해도 - 상태코드 200, 내 팔로워 목록에 {상대방 닉네임} 반환")
    @Test
    void test2() throws Exception {
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);
        //given
        String expression = "$.data[?(@.nickname == '%s')]";

        User ddalgi = userRepository.findByUsername("딸기").get();
        Long ddalgiId = ddalgi.getId();
        mvc.perform(MockMvcRequestBuilders.post("/friendship/followings/" + ddalgiId)
                .header("Authorization", subackToken));

        //당근 유저 -> 딸기 팔로우
       MvcResult result = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"danguen@gmail.com\", \"password\": \"123456Aa9\"}")).andReturn();

        String danguenToken = result.getResponse().getHeader("Authorization");

        User danguen = userRepository.findByUsername("당근").get();
        Long danguenId = danguen.getId();
        mvc.perform(MockMvcRequestBuilders.post("/friendship/followings/" + danguenId)
                .header("Authorization", danguenToken));

        //when
        mvc.perform(MockMvcRequestBuilders.get("/friendship/followers")
                        .header("Authorization", ddalgiToken))
                //then
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath(expression,"수박", "당근").exists());
    }
}
