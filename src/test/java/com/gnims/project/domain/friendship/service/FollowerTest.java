package com.gnims.project.domain.friendship.service;

import com.gnims.project.domain.friendship.repository.FriendshipRepository;
import com.gnims.project.domain.notification.repository.NotificationRepository;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 팔로워 조회 TEST
 */
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class FollowerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FriendshipRepository friendshipRepository;

    @Autowired
    NotificationRepository notificationRepository;

    String myToken = null;
    String user1Token = null;
    String user2Token = null;

    @Autowired
    PlatformTransactionManager transactionManager;

    @BeforeEach
    void beforeEach() throws Exception {
        makeUser();

        MvcResult result = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}")).andReturn();

        myToken = result.getResponse().getHeader("Authorization");

        MvcResult result2 = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"suback@gmail.com\", \"password\": \"123456aA9\"}")).andReturn();

        user1Token = result2.getResponse().getHeader("Authorization");

        MvcResult result3 = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"danguen@gmail.com\", \"password\": \"123456aA9\"}")).andReturn();

        user2Token = result3.getResponse().getHeader("Authorization");
    }

    @AfterEach
    void afterEach() {
        notificationRepository.deleteAll();
        friendshipRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("상대방이 나를 팔로우할 시 - 상태코드 200, 내 팔로워 목록에 {상대방 이름} 반환")
    @Test
    void test1() throws Exception {
        //given
        String expression = "$.data[?(@.username == '%s')]";

        User ddalgi = userRepository.findByNickname("딸기").get();
        Long ddalgiId = ddalgi.getId();
        mvc.perform(MockMvcRequestBuilders.post("/friendship/followings/" + ddalgiId)
                .header("Authorization", user1Token));

        //when
        mvc.perform(MockMvcRequestBuilders.get("/friendship/followers")
                .header("Authorization", myToken))

                //then
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath(expression,"박땡땡").exists());
    }

    @DisplayName("여러명이 나를 팔로우해도 - 상태코드 200, 내 팔로워 목록에 {상대방 이름} 반환")
    @Test
    void test2() throws Exception {
        //given
        String expression = "$.data[?(@.username == '%s')]";

        User ddalgi = userRepository.findByNickname("딸기").get();
        Long ddalgiId = ddalgi.getId();
        mvc.perform(MockMvcRequestBuilders.post("/friendship/followings/" + ddalgiId)
                .header("Authorization", user1Token));

        //당근 유저 -> 딸기 팔로우
       MvcResult result = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"danguen@gmail.com\", \"password\": \"123456aA9\",\"socialCode\": \"AUTH\"}")).andReturn();

        String danguenToken = result.getResponse().getHeader("Authorization");

        User danguen = userRepository.findByNickname("당근").get();
        Long danguenId = danguen.getId();
        mvc.perform(MockMvcRequestBuilders.post("/friendship/followings/" + danguenId)
                .header("Authorization", danguenToken));

        //when
        mvc.perform(MockMvcRequestBuilders.get("/friendship/followers")
                        .header("Authorization", myToken))
                //then
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath(expression,"박땡땡", "김땡땡").exists());
    }

    @DisplayName("팔로워 수 조회 성공 시 " +
            "상태 코드 200 " +
            "자신을 팔로우(FollowStatus : ACTIVE, INIT)하는 사용자의 수가 반환된다.")
    @Test
    void test4() throws Exception {
        //given
        User myself = userRepository.findByNickname("딸기").get();
        Long myselfId = myself.getId();

        //when
        // FollowStatus : ACTIVE - user1이 나를 팔로우
        for (int i = 0; i < 3; i++) {
            mvc.perform(post("/friendship/followings/"+ myselfId).header("Authorization", user1Token));
        }
        // FollowStatus : INIT - user2가 나를 팔로우
        mvc.perform(post("/friendship/followings/"+ myselfId).header("Authorization", user2Token));

        //when
        mvc.perform(get("/friendship/followers/counting").header("Authorization", myToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(2));
    }

    private void makeUser() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "data", "", "application/json",
                "{\"nickname\" : \"딸기\",\"username\": \"이땡땡\", \"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}".getBytes());
        mvc.perform(multipart("/auth/signup")
                .file(file1));

        MockMultipartFile file2 = new MockMultipartFile(
                "data", "", "application/json",
                "{\"nickname\" : \"당근\",\"username\": \"김땡땡\", \"email\": \"danguen@gmail.com\", \"password\": \"123456aA9\"}".getBytes());
        mvc.perform(multipart("/auth/signup")
                .file(file2));

        MockMultipartFile file3 = new MockMultipartFile(
                "data", "", "application/json",
                "{\"nickname\" : \"수박\",\"username\": \"박땡땡\", \"email\": \"suback@gmail.com\", \"password\": \"123456aA9\"}".getBytes());
        mvc.perform(multipart("/auth/signup")
                .file(file3));

        MockMultipartFile file4 = new MockMultipartFile(
                "data", "", "application/json",
                "{\"nickname\" : \"참외\",\"username\": \"최땡땡\", \"email\": \"chamwhe@gmail.com\", \"password\": \"123456aA9\"}".getBytes());
        mvc.perform(multipart("/auth/signup")
                .file(file4));
    }


}
