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
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 팔로잉 조회 API 테스트
 */
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class FollowingTest {
    @Autowired
    MockMvc mvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    FriendshipRepository friendshipRepository;
    @Autowired
    NotificationRepository notificationRepository;
    String mytoken = null;

    @BeforeEach
    void beforeEach() throws Exception {
        makeUser();

        MvcResult result = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}")).andReturn();

        mytoken = result.getResponse().getHeader("Authorization");
    }

    @AfterEach
    void afterEach() {
        notificationRepository.deleteAll();
        friendshipRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("최초 팔로우 상태(INIT)일 시 - 상태 코드 200, 'username' : {팔로잉 이름} 반환")
    @Test
    void test1() throws Exception {
        //given
        String expression = "$.data[?(@.username == '%s')]";
        String followingNickname = "박땡땡";

        User user = userRepository.findByNickname("수박").get();
        Long userId = user.getId();

        // 최초 팔로우 - FollowStatus == INIT
        mvc.perform(post("/friendship/followings/"+ userId).header("Authorization", mytoken));

        //when
        mvc.perform(get("/friendship/followings")
                //then
                .header("Authorization", mytoken))
                .andExpect(jsonPath(expression, followingNickname).exists());
    }

    @DisplayName("언팔로우 상태(INACTIVE)일 시 - 상태 코드 200, 'username' : {팔로잉 이름} 이 존재하지 않아야 한다.")
    @Test
    void test2() throws Exception {
        //given
        String expression = "$.data[?(@.username == '%s')]";
        String followingNickname = "박땡땡";

        User user = userRepository.findByNickname("수박").get();
        Long userId = user.getId();

        // 최초 팔로우 - FollowStatus == INIT
        mvc.perform(post("/friendship/followings/"+ userId).header("Authorization", mytoken));
        // 팔로우 취소 - FollowStatus == ACTIVE
        mvc.perform(post("/friendship/followings/"+ userId).header("Authorization", mytoken));

        //when
        mvc.perform(get("/friendship/followings")
                        //then
                        .header("Authorization", mytoken))
                .andExpect(jsonPath(expression, followingNickname).doesNotExist());
    }

    @DisplayName("최초 팔로우 상태(ACTIVE)일 시 - 상태 코드 200, 'username' : {팔로잉 이름} 반환")
    @Test
    void test3() throws Exception {
        //given
        String expression = "$.data[?(@.username == '%s')]";
        String followingNickname = "박땡땡";

        User user = userRepository.findByNickname("수박").get();
        Long userId = user.getId();

        // 최초 팔로우 - FollowStatus == INIT
        mvc.perform(post("/friendship/followings/"+ userId).header("Authorization", mytoken));
        // 팔로우 취소 - FollowStatus == ACTIVE
        mvc.perform(post("/friendship/followings/"+ userId).header("Authorization", mytoken));
        // 다시 팔로우 - FollowStatus == INACTIVE
        mvc.perform(post("/friendship/followings/"+ userId).header("Authorization", mytoken));

        //when
        mvc.perform(get("/friendship/followings")
                //then
                .header("Authorization", mytoken))
                .andExpect(jsonPath(expression, followingNickname).exists());
    }

    @DisplayName("팔로잉 수 조회 성공 시 " +
            "상태 코드 200 " +
            "자신이 팔로우(FollowStatus : ACTIVE, INIT)하는 사용자의 수가 반환된다.")
    @Test
    void test4() throws Exception {
        //given
        User user1 = userRepository.findByNickname("수박").get();
        Long userId1 = user1.getId();
        // FollowStatus : ACTIVE
        for (int i = 0; i < 3; i++) {
            mvc.perform(post("/friendship/followings/"+ userId1).header("Authorization", mytoken));
        }
        // FollowStatus : INIT
        User user2 = userRepository.findByNickname("당근").get();
        Long userId2 = user2.getId();
        mvc.perform(post("/friendship/followings/"+ userId2).header("Authorization", mytoken));

        //when
        mvc.perform(get("/friendship/followings/counting").header("Authorization", mytoken))
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
