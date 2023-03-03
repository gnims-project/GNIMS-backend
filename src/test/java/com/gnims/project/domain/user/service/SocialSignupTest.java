package com.gnims.project.domain.user.service;

import com.gnims.project.domain.user.entity.SocialCode;
import com.gnims.project.domain.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;

import static com.gnims.project.share.message.ExceptionMessage.*;
import static com.gnims.project.share.message.ResponseMessage.SIGNUP_SUCCESS_MESSAGE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

/**
 * 소셜 회원가입 TEST
 * */
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class SocialSignupTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository userRepository;

    @Value("${profile.image}")
    private String defaultImage;

    String invalidToken = "test";

    @AfterEach
    void afterEach() throws Exception {
        userRepository.deleteAll();
    }

    @DisplayName("카카오가입 성공(이미지 있을 때) - 상태코드 201, 성공 메세지를 반환, db에 저장")
    @Test
    void 카카오가입성공테스트1() throws Exception {

        MockMultipartFile kakaoFile = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"딸기\", \"username\": \"이땡땡\", \"email\": \"ddalgi@kakao.com\", \"socialCode\": \"KAKAO\"}".getBytes());

        final String fileName = "르탄이"; //파일명
        final String contentType = "png"; //파일타입
        final String filePath = "src/test/resources/image/"+fileName+"."+contentType; //파일경로
        FileInputStream fileInputStream = new FileInputStream(filePath);

        //Mock파일생성
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", //name
                fileName + "." + contentType, //originalFilename
                contentType,
                fileInputStream
        );

        /**
         * S3 프리티어 제한으로 인해
         * 테스트 때마다 S3에 이미지 쌓이는 것을 방지
         * 주석처리
         * */

        //이미지 있을 때 - kakao
        mvc.perform(multipart("/social/signup")
                        /*.file(imageFile)*/.file(kakaoFile).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(SIGNUP_SUCCESS_MESSAGE));

        //DB에 저장 됨
        Assertions.assertThat(userRepository.findByNickname("딸기").get()).isNotNull();

        /*
            //이미지는 UUID로 저장이 되기 때문에
            //기본이미지가 아닌 지로 판별
            Assertions.assertThat(userRepository.findByNickname("딸기").get().getProfileImage()).isNotEqualTo(defaultImage);
        */
    }

    @DisplayName("네이버가입 성공(이미지 있을 때) - 상태코드 201, 성공 메세지를 반환, db에 저장")
    @Test
    void 네이버가입성공테스트1() throws Exception {

        MockMultipartFile naverFile = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"포도\", \"username\": \"김땡땡\", \"email\": \"orange@naver.com\", \"socialCode\": \"NAVER\"}".getBytes());

        final String fileName = "르탄이"; //파일명
        final String contentType = "png"; //파일타입
        final String filePath = "src/test/resources/image/"+fileName+"."+contentType; //파일경로
        FileInputStream fileInputStream = new FileInputStream(filePath);

        //Mock파일생성
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", //name
                fileName + "." + contentType, //originalFilename
                contentType,
                fileInputStream
        );

        /**
         * S3 프리티어 제한으로 인해
         * 테스트 때마다 S3에 이미지 쌓이는 것을 방지
         * 주석처리
         * */

        //이미지 있을 때 - naver
        mvc.perform(multipart("/social/signup")
                        /*.file(imageFile)*/.file(naverFile).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(SIGNUP_SUCCESS_MESSAGE));

        //DB에 저장 됨
        Assertions.assertThat(userRepository.findByNickname("포도").get()).isNotNull();

        /*
            //이미지는 UUID로 저장이 되기 때문에
            //기본이미지가 아닌 지로 판별
            Assertions.assertThat(userRepository.findByNickname("포도").get().getProfileImage()).isNotEqualTo(defaultImage);
        */
    }

    @DisplayName("카카오가입 성공(이미지 null일 때) - 상태코드 201, 성공 메세지를 반환, db에 저장")
    @Test
    void 카카오가입성공테스트2() throws Exception {

        MockMultipartFile kakaoFile = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"딸기\", \"username\": \"이땡땡\", \"email\": \"ddalgi@kakao.com\", \"socialCode\": \"KAKAO\"}".getBytes());

        //이미지 없을 때 - KAKAO
        mvc.perform(multipart("/social/signup")
                        .file(kakaoFile).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(SIGNUP_SUCCESS_MESSAGE));

        //DB에 저장 됨
        Assertions.assertThat(userRepository.findByNickname("딸기").get()).isNotNull();

        //기본이미지 인지 판별
        Assertions.assertThat(userRepository.findByNickname("딸기").get().getProfileImage()).isEqualTo(defaultImage);
    }

    @DisplayName("네이버가입 성공(이미지 null일 때) - 상태코드 201, 성공 메세지를 반환, db에 저장")
    @Test
    void 네이버가입성공테스트2() throws Exception {

        MockMultipartFile naverFile = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"포도\", \"username\": \"김땡땡\", \"email\": \"orange@naver.com\", \"socialCode\": \"NAVER\"}".getBytes());

        //이미지 없을 때 - NAVER
        mvc.perform(multipart("/social/signup")
                        .file(naverFile).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(SIGNUP_SUCCESS_MESSAGE));

        //DB에 저장 됨
        Assertions.assertThat(userRepository.findByNickname("포도").get()).isNotNull();

        //기본이미지 인지 판별
        Assertions.assertThat(userRepository.findByNickname("포도").get().getProfileImage()).isEqualTo(defaultImage);
    }

    @DisplayName("소셜가입 성공, 토큰이 같이 왓을 시 - 상태코드 201, 성공 메세지를 반환, db에 저장")
    @Test
    void 소셜가입토큰성공테스트() throws Exception {

        MockMultipartFile kakaoFile = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"포도\", \"username\": \"김땡땡\", \"email\": \"orange@kakao.com\", \"socialCode\": \"KAKAO\"}".getBytes());

        mvc.perform(multipart("/social/signup")
                        .file(kakaoFile).characterEncoding("utf-8").header("Authorization", invalidToken))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(SIGNUP_SUCCESS_MESSAGE));

        //DB에 저장 됨
        Assertions.assertThat(userRepository.findByNickname("포도").get()).isNotNull();
    }

    @DisplayName("소셜가입 시 닉네임 중복 - 상태코드 400, 에러 메세지를 반환, db에 저장 실패")
    @Test
    void 회원가입실패테스트1() throws Exception {

        //중복 체크를 위해 db에 넣을 데이터
        MockMultipartFile successFile = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"딸기\",\"username\": \"이땡땡\", \"email\": \"ddalgi@kakao.com\", \"socialCode\": \"KAKAO\"}".getBytes());

        //닉네임 중복
        MockMultipartFile failFile = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"딸기\",\"username\": \"김땡땡\", \"email\": \"ddalgi2@kakao.com\", \"socialCode\": \"KAKAO\"}".getBytes());

        mvc.perform(multipart("/social/signup")
                .file(successFile).characterEncoding("utf-8"));

        //닉네임 중복
        mvc.perform(multipart("/social/signup")
                        .file(failFile).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(DUPLICATE_NICKNAME));

        //DB에 저장되었는지 검사
        Assertions.assertThat(userRepository.findByEmail(SocialCode.KAKAO.getValue() + "ddalgi2@kakao.com")).isEmpty();
    }

    /**
     * Client에서, 소셜에서 받은 이메일을 넣어주지만
     * 포스트맨 등으로 API 직접 접근 시
     * 이메일 중복 체크 필요
     * */
    @DisplayName("소셜가입 시 이메일 중복 - 상태코드 400, 에러 메세지를 반환, db에 저장 실패")
    @Test
    void 회원가입실패테스트2() throws Exception {

        //중복 체크를 위해 db에 넣을 데이터
        MockMultipartFile successFile = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"딸기\",\"username\": \"이땡땡\", \"email\": \"ddalgi@kakao.com\", \"socialCode\": \"KAKAO\"}".getBytes());

        //이메일 중복
        MockMultipartFile failFile = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"김밥\",\"username\": \"박땡땡\", \"email\": \"ddalgi@kakao.com\", \"socialCode\": \"KAKAO\"}".getBytes());

        mvc.perform(multipart("/social/signup")
                .file(successFile).characterEncoding("utf-8"));

        //이메일 중복
        mvc.perform(multipart("/social/signup")
                        .file(failFile).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(ALREADY_REGISTERED_EMAIL));

        //DB에 저장되었는지 검사
        Assertions.assertThat(userRepository.findByNickname("김밥")).isEmpty();
    }

    @DisplayName("소셜가입 시 닉네임 값이 null 혹은 정규식 불 일치 - 상태코드 400, 에러 메세지를 반환, db에 저장 실패")
    @Test
    void 회원가입실패테스트3() throws Exception {

        //닉네임 null 값
        MockMultipartFile failFile1 = new MockMultipartFile("data", "", "application/json", "{\"username\": \"신땡땡\", \"email\": \"orange@kakao.com\", \"socialCode\": \"KAKAO\"}".getBytes());

        //정규식 불일치
        MockMultipartFile failFile2 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"짱구!\",\"username\": \"신땡땡\", \"email\": \"orange@kakao.com\", \"socialCode\": \"KAKAO\"}".getBytes());

        //닉네임 null 값
        mvc.perform(multipart("/social/signup")
                        .file(failFile1).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messages[*]").value(NICKNAME_EMPTY_MESSAGE));
        Assertions.assertThat(userRepository.findByEmail(SocialCode.KAKAO.getValue() + "orange@kakao.com")).isEmpty();

        //닉네임 정규식 불 일치
        mvc.perform(multipart("/social/signup")
                        .file(failFile2).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messages[*]").value(NICKNAME_ERROR_MESSAGE));
        Assertions.assertThat(userRepository.findByEmail(SocialCode.KAKAO.getValue() + "orange@kakao.com")).isEmpty();
    }

    @DisplayName("소셜가입 시 이메일 값이 null 혹은 정규식 불 일치 - 상태코드 400, 에러 메세지를 반환, db에 저장 실패")
    @Test
    void 회원가입실패테스트4() throws Exception {

        //닉네임 null 값
        MockMultipartFile failFile1 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"짱구\", \"username\": \"신땡땡\", \"socialCode\": \"KAKAO\"}".getBytes());

        //정규식 불일치
        MockMultipartFile failFile2 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"짱구\",\"username\": \"신땡땡\", \"email\": \"orange@kakaocom\", \"socialCode\": \"KAKAO\"}".getBytes());

        //이메일 null 값
        mvc.perform(multipart("/social/signup")
                        .file(failFile1).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messages[*]").value(EMAIL_EMPTY_MESSAGE));
        Assertions.assertThat(userRepository.findByNickname("짱구")).isEmpty();

        //이메일 정규식 불 일치
        mvc.perform(multipart("/social/signup")
                        .file(failFile2).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messages[*]").value(EMAIL_ERROR_MESSAGE));
        Assertions.assertThat(userRepository.findByNickname("짱구")).isEmpty();
    }

    @DisplayName("소셜가입 시 이름 값이 null 혹은 정규식 불 일치 - 상태코드 400, 에러 메세지를 반환, db에 저장 실패")
    @Test
    void 회원가입실패테스트5() throws Exception {

        //이름 null 값
        MockMultipartFile failFile1 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"짱구\", \"email\": \"orange@gmail.com\", \"password\": \"1234aA5678\"}".getBytes());

        //정규식 불일치
        MockMultipartFile failFile2 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"짱구\", \"username\": \"신땡땡1\", \"email\": \"orange@gmail.com\", \"password\": \"1234aA5678\"}".getBytes());

        //이름 null 값
        mvc.perform(multipart("/social/signup")
                        .file(failFile1).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messages[*]").value(USERNAME_EMPTY_MESSAGE));
        Assertions.assertThat(userRepository.findByNickname("짱구")).isEmpty();

        //이름 정규식 불 일치
        mvc.perform(multipart("/social/signup")
                        .file(failFile2).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messages[*]").value(USERNAME_ERROR_MESSAGE));
        Assertions.assertThat(userRepository.findByNickname("짱구")).isEmpty();
    }


    /**
     * 일반적인 접근이 아닌
     * 포스트 맨등의 접근시에 발생 할 수 있음
     * */
    @DisplayName("소셜가입 시 소셜 코드 값이 null 혹은 정규식 불 일치 - 상태코드 500, db에 저장 실패")
    @Test
    void 회원가입실패테스트6() throws Exception {

        //소셜 코드 null 값
        MockMultipartFile failFile1 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"짱구\", \"username\": \"신땡땡\", \"email\": \"orange@kakao.com\"}".getBytes());

        //정규식 불일치
        MockMultipartFile failFile2 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"짱구\", \"username\": \"신땡땡\", \"email\": \"orange@kakao.com\", \"socialCode\": \"test\"}".getBytes());

        //소셜 코드 null 값
        mvc.perform(multipart("/social/signup")
                        .file(failFile1).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
        Assertions.assertThat(userRepository.findByNickname("짱구")).isEmpty();

        //소셜 코드 정규식 불 일치
        mvc.perform(multipart("/social/signup")
                        .file(failFile2).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
        Assertions.assertThat(userRepository.findByNickname("짱구")).isEmpty();
    }

    @DisplayName("소셜가입 시 (여러) 값이 null 혹은 정규식 불 일치 - 상태코드 400, 에러 메세지를 반환, db에 저장 실패")
    @Test
    void 회원가입실패테스트7() throws Exception {

        //이메일, 닉네임, 이름, 비밀번호 중 null 값
        MockMultipartFile failFile1 = new MockMultipartFile("data", "", "application/json", "{\"email\": \"orange@kakao.com\", \"socialCode\": \"KAKAO\"}".getBytes());

        //정규식 불일치
        MockMultipartFile failFile2 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"오렌지1234567890\", \"username\": \"신땡땡1\", \"email\": \"orange@kakao.com\", \"socialCode\": \"KAKAO\"}".getBytes());

        //닉네임, 이름 null 값
        mvc.perform(multipart("/social/signup")
                        .file(failFile1).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[?(@.messages == ['%s', '%s'])]", NICKNAME_EMPTY_MESSAGE, USERNAME_EMPTY_MESSAGE).exists());
        Assertions.assertThat(userRepository.findByEmail(SocialCode.KAKAO.getValue() + "orange@kakao.com").isEmpty());

        //이메일, 닉네임, 이름 정규식 불 일치
        mvc.perform(multipart("/social/signup")
                        .file(failFile2).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[?(@.messages == ['%s', '%s'])]", USERNAME_ERROR_MESSAGE, NICKNAME_ERROR_MESSAGE).exists());
        Assertions.assertThat(userRepository.findByEmail(SocialCode.KAKAO.getValue() + "orange@kakao.com").isEmpty());
    }
}