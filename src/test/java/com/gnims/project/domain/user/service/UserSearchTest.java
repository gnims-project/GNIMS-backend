package com.gnims.project.domain.user.service;

import com.gnims.project.domain.friendship.repository.FriendshipRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.gnims.project.share.message.ResponseMessage.USER_SEARCH_SUCCESS_MESSAGE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * 유저 검색 TEST
 * */
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class UserSearchTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FriendshipRepository friendshipRepository;

    String token = null;

    @BeforeEach
    void beforeEach() throws Exception {
        MockMultipartFile signupFile1 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"딸기\",\"username\": \"짱구\", \"email\": \"test@naver.com\", \"password\": \"123456aA9\"}".getBytes());
        MockMultipartFile signupFile2 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"포도\",\"username\": \"유리\", \"email\": \"test1@naver.com\", \"password\": \"123456aA9\"}".getBytes());
        MockMultipartFile signupFile3 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"오렌지\",\"username\": \"철수\", \"email\": \"test2@naver.com\", \"password\": \"123456aA9\"}".getBytes());
        MockMultipartFile signupFile4 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"자두\",\"username\": \"훈이\", \"email\": \"test3@naver.com\", \"password\": \"123456aA9\"}".getBytes());
        MockMultipartFile signupFile5 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"사과\",\"username\": \"맹구\", \"email\": \"test4@naver.com\", \"password\": \"123456aA9\"}".getBytes());
        MockMultipartFile signupFile6 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"체리\",\"username\": \"흰둥이\", \"email\": \"test5@naver.com\", \"password\": \"123456aA9\"}".getBytes());
        MockMultipartFile signupFile7 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"두리안\",\"username\": \"유리\", \"email\": \"test6@naver.com\", \"password\": \"123456aA9\"}".getBytes());


        mvc.perform(multipart("/auth/signup").file(signupFile1));
        mvc.perform(multipart("/auth/signup").file(signupFile2));
        mvc.perform(multipart("/auth/signup").file(signupFile3));
        mvc.perform(multipart("/auth/signup").file(signupFile4));
        mvc.perform(multipart("/auth/signup").file(signupFile5));
        mvc.perform(multipart("/auth/signup").file(signupFile6));
        mvc.perform(multipart("/auth/signup").file(signupFile7));

        MvcResult result = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"test@naver.com\", \"password\": \"123456aA9\"}")).andReturn();

        token = result.getResponse().getHeader("Authorization");
    }

    @AfterEach
    void afterEach(){
        friendshipRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("유저 검색 시 유저 리스트 조회 - 상태코드 200, 유저 리스트와 성공 메세지를 반환")
    @Test
    void 검색성공테스트() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/users/search").header("Authorization", token)
                        .param("username", "철수").param("page", "0").param("size", "5"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(USER_SEARCH_SUCCESS_MESSAGE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[*].username").value("철수"));
    }

    @DisplayName("유저 부분 검색 시 유저 리스트 조회 - 상태코드 200, 유저 리스트와 성공 메세지를 반환")
    @Test
    void 검색성공테스트2() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/users/search").header("Authorization", token)
                        .param("username", "이").param("page", "0").param("size", "5"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(USER_SEARCH_SUCCESS_MESSAGE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[*].username").value(new ArrayList<>(List.of(new String[]{"훈이", "흰둥이"}))));
    }

    @DisplayName("유저 검색 시 중복된 이름 조회 - 상태코드 200, 유저 리스트와 성공 메세지를 반환")
    @Test
    void 유저중복성공테스트() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/users/search").header("Authorization", token)
                        .param("username", "유").param("page", "0").param("size", "5"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(USER_SEARCH_SUCCESS_MESSAGE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[*].username").value(new ArrayList<>(List.of(new String[]{"유리", "유리"}))));
    }

    @DisplayName("유저 검색 시 본인 이름 조회 x - 상태코드 200, 본인제외 유저 리스트와 성공 메세지를 반환")
    @Test
    void 본인제외성공테스트() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/users/search").header("Authorization", token)
                        .param("username", "짱구").param("page", "0").param("size", "5"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(USER_SEARCH_SUCCESS_MESSAGE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[*].username").isEmpty());
    }

    @DisplayName("유저 검색 시 팔로우 여부 조회 - 상태코드 200, 유저 리스트와 성공 메세지를 반환")
    @Test
    void 유저검색팔로우여부테스트() throws Exception {
        //닉네임 오렌지 = 철수
        String uri = "/friendship/followings/" + userRepository.findByNickname("오렌지").get().getId();

        mvc.perform(MockMvcRequestBuilders.post(uri).header("Authorization", token));

        //팔로잉 유저
        mvc.perform(MockMvcRequestBuilders.get("/users/search").header("Authorization", token)
                        .param("username", "철수").param("page", "0").param("size", "5"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(USER_SEARCH_SUCCESS_MESSAGE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[*].username").value("철수"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[*].isFollowed").value(true));

        //팔로잉 아닌 유저
        mvc.perform(MockMvcRequestBuilders.get("/users/search").header("Authorization", token)
                        .param("username", "맹구").param("page", "0").param("size", "5"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(USER_SEARCH_SUCCESS_MESSAGE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[*].username").value("맹구"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[*].isFollowed").value(false));
    }

}