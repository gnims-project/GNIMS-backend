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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * 팔로잉 조회 API 테스트
 */
@AutoConfigureMockMvc
@SpringBootTest
public class FollowingTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FriendshipRepository friendshipRepository;

    String authorization = null;

    @Autowired
    PlatformTransactionManager transactionManager;

    TransactionStatus status = null;

    @BeforeEach
    void beforeEach() throws Exception {
        mvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\" : \"딸기\", \"email\": \"ddalgi@gmail.com\", \"password\": \"123456\"}"));

        mvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\" : \"당근\", \"email\": \"danguen@gmail.com\", \"password\": \"123456\"}"));

        mvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\" : \"수박\", \"email\": \"suback@gmail.com\", \"password\": \"123456\"}"));

        mvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\" : \"참외\", \"email\": \"chamwhe@gmail.com\", \"password\": \"123456\"}"));

        MvcResult result = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"ddalgi@gmail.com\", \"password\": \"123456\"}")).andReturn();

        authorization = result.getResponse().getHeader("Authorization");
    }

    @AfterEach
    void afterEach() {
        friendshipRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Transactional
    @DisplayName("최초 팔로우 상태(INIT)일 시 - 상태 코드 200, 'nickname' : {팔로잉 닉네임} 반환")
    @Test
    void test1() throws Exception {
        //given
        String expression = "$.data[?(@.nickname == '%s')]";
        String followingNickname = "수박";

        User user = userRepository.findByUsername("수박").get();
        Long userId = user.getId();

        // 최초 팔로우 - FollowStatus == INIT
        mvc.perform(post("/friendship/followings/"+ userId).header("Authorization", authorization));

        //when
        mvc.perform(get("/friendship/followings")
                //then
                .header("Authorization", authorization))
                .andExpect(MockMvcResultMatchers.jsonPath(expression, followingNickname).exists());
    }

    @DisplayName("팔로우 상태(INACTIVE)일 시 - 상태 코드 200, 'nickname' : {팔로잉 닉네임} 이 존재하지 않아야 한다.")
    @Test
    void test2() throws Exception {
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);

        //given
        String expression = "$.data[?(@.nickname == '%s')]";
        String followingNickname = "수박";

        User user = userRepository.findByUsername("수박").get();
        Long userId = user.getId();

        // 최초 팔로우 - FollowStatus == INIT
        mvc.perform(post("/friendship/followings/"+ userId).header("Authorization", authorization));
        // 팔로우 취소 - FollowStatus == ACTIVE
        mvc.perform(post("/friendship/followings/"+ userId).header("Authorization", authorization));

        //when
        mvc.perform(get("/friendship/followings")
                        //then
                        .header("Authorization", authorization))
                .andExpect(MockMvcResultMatchers.jsonPath(expression, followingNickname).doesNotExist());
    }

    @DisplayName("최초 팔로우 상태(ACTIVE)일 시 - 상태 코드 200, 'nickname' : {팔로잉 닉네임} 반환")
    @Test
    void test3() throws Exception {
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        transactionManager.commit(status);

        //given
        String expression = "$.data[?(@.nickname == '%s')]";
        String followingNickname = "수박";

        User user = userRepository.findByUsername("수박").get();
        Long userId = user.getId();

        // 최초 팔로우 - FollowStatus == INIT
        mvc.perform(post("/friendship/followings/"+ userId).header("Authorization", authorization));
        // 팔로우 취소 - FollowStatus == ACTIVE
        mvc.perform(post("/friendship/followings/"+ userId).header("Authorization", authorization));
        // 다시 팔로우 - FollowStatus == INACTIVE
        mvc.perform(post("/friendship/followings/"+ userId).header("Authorization", authorization));

        //when
        mvc.perform(get("/friendship/followings")
                //then
                .header("Authorization", authorization))
                .andExpect(MockMvcResultMatchers.jsonPath(expression, followingNickname).exists());
    }
}
