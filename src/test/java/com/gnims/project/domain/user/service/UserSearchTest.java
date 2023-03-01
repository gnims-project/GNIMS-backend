package com.gnims.project.domain.user.service;

import com.gnims.project.domain.user.repository.UserRepository;
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
    PlatformTransactionManager transactionManager;

    TransactionStatus status = null;

    String token = null;

    @BeforeEach
    void beforeEach() throws Exception {

        MockMultipartFile signupFile1 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"하얀가을\",\"username\": \"찐계정\", \"email\": \"test123@naver.com\", \"password\": \"123456aA9\"}".getBytes());
        MockMultipartFile signupFile2 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"하얀가을1\",\"username\": \"이땡땡\", \"email\": \"test1@naver.com\", \"password\": \"123456aA9\"}".getBytes());
        MockMultipartFile signupFile3 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"하얀가2을\",\"username\": \"김땡땡\", \"email\": \"test2@naver.com\", \"password\": \"123456aA9\"}".getBytes());
        MockMultipartFile signupFile4 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"하얀3가을\",\"username\": \"박땡땡\", \"email\": \"test3@naver.com\", \"password\": \"123456aA9\"}".getBytes());
        MockMultipartFile signupFile5 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"하4얀가을\",\"username\": \"정땡땡\", \"email\": \"test4@naver.com\", \"password\": \"123456aA9\"}".getBytes());
        MockMultipartFile signupFile6 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"5하얀가을\",\"username\": \"유땡땡\", \"email\": \"test5@naver.com\", \"password\": \"123456aA9\"}".getBytes());
        MockMultipartFile signupFile7 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"하a얀1가3을\",\"username\": \"신땡땡\", \"email\": \"test6@naver.com\", \"password\": \"123456aA9\"}".getBytes());
        MockMultipartFile signupFile8 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"abc하얀가을\",\"username\": \"황땡땡\", \"email\": \"test7@naver.com\", \"password\": \"123456aA9\"}".getBytes());
        MockMultipartFile signupFile9 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"하얀가을abc\",\"username\": \"성땡땡\", \"email\": \"test8@naver.com\", \"password\": \"123456aA9\"}".getBytes());
        MockMultipartFile signupFile10 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"하a얀c가1을\",\"username\": \"하땡땡\", \"email\": \"test9@naver.com\", \"password\": \"123456aA9\"}".getBytes());


        mvc.perform(multipart("/auth/signup").file(signupFile1));
        mvc.perform(multipart("/auth/signup").file(signupFile2));
        mvc.perform(multipart("/auth/signup").file(signupFile3));
        mvc.perform(multipart("/auth/signup").file(signupFile4));
        mvc.perform(multipart("/auth/signup").file(signupFile5));
        mvc.perform(multipart("/auth/signup").file(signupFile6));
        mvc.perform(multipart("/auth/signup").file(signupFile7));
        mvc.perform(multipart("/auth/signup").file(signupFile8));
        mvc.perform(multipart("/auth/signup").file(signupFile9));
        mvc.perform(multipart("/auth/signup").file(signupFile10));

        MvcResult result = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"test123@naver.com\", \"password\": \"123456aA9\"}")).andReturn();

        token = result.getResponse().getHeader("Authorization");
    }

    @DisplayName("단어 검색 시 문자 순서 맞는 유저 리스트 조회 - 상태코드 200, 유저 리스트와 성공 메세지를 반환")
    @Test
    void 일반단어성공테스트() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get("/users/search").header("Authorization", token)
                        .param("username", "정땡땡").param("page", "0").param("size", "5"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(USER_SEARCH_SUCCESS_MESSAGE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[?(@.data[*].username)]", "정땡땡").exists());
    }

    @DisplayName("단어 검색 시 문자 순서 맞는 유저 리스트 조회 - 상태코드 200, 유저 리스트와 성공 메세지를 반환")
    @Test
    void 일반단어성공테스트결과여러개() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get("/users/search").header("Authorization", token)
                        .param("username", "땡땡").param("page", "0").param("size", "5"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(USER_SEARCH_SUCCESS_MESSAGE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[?(@.data[*].username)]", "하땡땡", "성땡땡", "황땡땡", "신땡땡", "유땡땡").exists());
    }

//    @DisplayName("초성 단어 검색 시 문자 순서 맞는 유저 리스트 조회 - 상태코드 200, 유저 리스트와 성공 메세지를 반환")
//    @Test
//    void 초성단어성공테스트() throws Exception {
//        String expression2 = "$.[?(@.data[*].nickname)]";
//
//        mvc.perform(MockMvcRequestBuilders.get("/users/search").header("Authorization", token)
//                        .param("nickname", "ㅎㅇㄱㅇ").param("number", "0"))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("유저 검색 성공"))
//                .andExpect(MockMvcResultMatchers.jsonPath(expression2, "하a얀c가1을", "하얀가을abc", "abc하얀가을", "하a얀1가3을", "5하얀가을").exists());
//    }
//
//    @DisplayName("문자 + 초성 단어 검색 시 문자 순서 맞는 유저 리스트 조회 - 상태코드 200, 유저 리스트와 성공 메세지를 반환")
//    @Test
//    void 혼합단어성공테스트() throws Exception {
//        String expression2 = "$.[?(@.data[*].nickname)]";
//
//        mvc.perform(MockMvcRequestBuilders.get("/users/search").header("Authorization", token)
//                        .param("nickname", "하cㅇ").param("number", "0"))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("유저 검색 성공"))
//                .andExpect(MockMvcResultMatchers.jsonPath(expression2, "하a얀c가1을").exists());
//    }

}