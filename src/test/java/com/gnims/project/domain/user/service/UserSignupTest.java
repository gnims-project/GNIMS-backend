package com.gnims.project.domain.user.service;

import com.gnims.project.domain.user.entity.SocialCode;
import com.gnims.project.domain.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
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
 * 일반 회원가입 TEST
 * */
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class UserSignupTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository userRepository;

    @Value("${profile.image}")
    private String defaultImage;

    @DisplayName("회원가입 성공(이미지 있을 때) - 상태코드 201, 성공 메세지를 반환, db에 저장")
    @Test
    void 회원가입성공테스트1() throws Exception {

        MockMultipartFile signupFile = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"딸기\",\"username\": \"이땡땡\", \"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}".getBytes());

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

        String expression = "$.[?(@.message == '%s')]";


        /**
         * S3 프리티어 제한으로 인해
         * 테스트 때마다 S3에 이미지 쌓이는 것을 방지
         * 주석처리
         * */

        //이미지 있을 때
        mvc.perform(multipart("/auth/signup")
                /*.file(imageFile)*/.file(signupFile).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath(expression, SIGNUP_SUCCESS_MESSAGE).exists());

        //DB에 저장 됨
        Assertions.assertThat(userRepository.findByNickname("딸기").get()).isNotNull();

        /*
            //이미지는 UUID로 저장이 되기 때문에
            //기본이미지가 아닌 지로 판별
            Assertions.assertThat(userRepository.findByNickname("딸기").get().getProfileImage()).isNotEqualTo(defaultImage);
        */
    }

    @DisplayName("회원가입 성공(이미지 null일 때) - 상태코드 201, 성공 메세지를 반환, db에 저장, 이미지는 기본 이미지 저장")
    @Test
    void 회원가입성공테스트2() throws Exception {

        MockMultipartFile signupFile = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"딸기\",\"username\": \"이땡땡\", \"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}".getBytes());

        String expression = "$.[?(@.message == '%s')]";

        //이미지 없을 때
        mvc.perform(multipart("/auth/signup")
                        .file(signupFile).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath(expression, SIGNUP_SUCCESS_MESSAGE).exists());

        //DB에 저장 됨
        Assertions.assertThat(userRepository.findByNickname("딸기").get()).isNotNull();

        //기본이미지 인지 판별
        Assertions.assertThat(userRepository.findByNickname("딸기").get().getProfileImage()).isEqualTo(defaultImage);
    }

    @DisplayName("회원가입 시 닉네임 중복 - 상태코드 400, 에러 메세지를 반환, db에 저장 실패")
    @Test
    void 회원가입실패테스트1() throws Exception {

        //중복 체크를 위해 db에 넣을 데이터
        MockMultipartFile successFile = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"딸기\",\"username\": \"이땡땡\", \"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}".getBytes());

        //닉네임 중복
        MockMultipartFile failFile = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"딸기\",\"username\": \"김땡땡\", \"email\": \"ddalgi2@gmail.com\", \"password\": \"123456aA9\"}".getBytes());

        String expression = "$.[?(@.message == '%s')]";

        mvc.perform(multipart("/auth/signup")
                .file(successFile).characterEncoding("utf-8"));

        //닉네임 중복
        mvc.perform(multipart("/auth/signup")
                        .file(failFile).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath(expression, DUPLICATE_NICKNAME).exists());

        //DB에 저장되었는지 검사
        Assertions.assertThat(userRepository.findByEmail(SocialCode.EMAIL.getValue() + "ddalgi2@gmail.com")).isEmpty();
    }

    @DisplayName("회원가입 시 이메일 중복 - 상태코드 400, 에러 메세지를 반환, db에 저장 실패")
    @Test
    void 회원가입실패테스트2() throws Exception {

        //중복 체크를 위해 db에 넣을 데이터
        MockMultipartFile successFile = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"딸기\",\"username\": \"이땡땡\", \"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}".getBytes());

        //이메일 중복
        MockMultipartFile failFile = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"김밥\",\"username\": \"박땡땡\", \"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}".getBytes());

        String expression = "$.[?(@.message == '%s')]";

        mvc.perform(multipart("/auth/signup")
                        .file(successFile).characterEncoding("utf-8"));

        //이메일 중복
        mvc.perform(multipart("/auth/signup")
                        .file(failFile).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath(expression, ALREADY_REGISTERED_EMAIL).exists());

        //DB에 저장되었는지 검사
        Assertions.assertThat(userRepository.findByNickname("김밥")).isEmpty();
    }

    @DisplayName("회원가입 시 닉네임 값이 null 혹은 정규식 불 일치 - 상태코드 400, 에러 메세지를 반환, db에 저장 실패")
    @Test
    void 회원가입실패테스트3() throws Exception {

        //닉네임 null 값
        MockMultipartFile failFile1 = new MockMultipartFile("data", "", "application/json", "{\"username\": \"신땡땡\", \"email\": \"orange@gmail.com\", \"password\": \"1234aA5678\"}".getBytes());

        //정규식 불일치
        MockMultipartFile failFile2 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"짱구!\",\"username\": \"신땡땡\", \"email\": \"orange@gmail.com\", \"password\": \"1234aA5678\"}".getBytes());

        String expression = "$.[?(@.messages == ['%s'])]";

        //닉네임 null 값
        mvc.perform(multipart("/auth/signup")
                        .file(failFile1).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath(expression, NICKNAME_EMPTY_MESSAGE).exists());
        Assertions.assertThat(userRepository.findByEmail(SocialCode.EMAIL.getValue() + "orange@gmail.com")).isEmpty();

        //닉네임 정규식 불 일치
        mvc.perform(multipart("/auth/signup")
                        .file(failFile2).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath(expression, NICKNAME_ERROR_MESSAGE).exists());
        Assertions.assertThat(userRepository.findByEmail(SocialCode.EMAIL.getValue() + "orange@gmail.com")).isEmpty();
    }

    @DisplayName("회원가입 시 이메일 값이 null 혹은 정규식 불 일치 - 상태코드 400, 에러 메세지를 반환, db에 저장 실패")
    @Test
    void 회원가입실패테스트4() throws Exception {

        //닉네임 null 값
        MockMultipartFile failFile1 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"짱구\", \"username\": \"신땡땡\", \"password\": \"1234aA5678\"}".getBytes());

        //정규식 불일치
        MockMultipartFile failFile2 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"짱구\",\"username\": \"신땡땡\", \"email\": \"orange@gmailcom\", \"password\": \"1234aA5678\"}".getBytes());

        String expression = "$.[?(@.messages == ['%s'])]";

        //이메일 null 값
        mvc.perform(multipart("/auth/signup")
                        .file(failFile1).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath(expression, EMAIL_EMPTY_MESSAGE).exists());
        Assertions.assertThat(userRepository.findByNickname("짱구")).isEmpty();

        //이메일 정규식 불 일치
        mvc.perform(multipart("/auth/signup")
                        .file(failFile2).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath(expression, EMAIL_ERROR_MESSAGE).exists());
        Assertions.assertThat(userRepository.findByNickname("짱구")).isEmpty();
    }

    @DisplayName("회원가입 시 이름 값이 null 혹은 정규식 불 일치 - 상태코드 400, 에러 메세지를 반환, db에 저장 실패")
    @Test
    void 회원가입실패테스트5() throws Exception {

        //이름 null 값
        MockMultipartFile failFile1 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"짱구\", \"email\": \"orange@gmail.com\", \"password\": \"1234aA5678\"}".getBytes());

        //정규식 불일치
        MockMultipartFile failFile2 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"짱구\", \"username\": \"신땡땡1\", \"email\": \"orange@gmail.com\", \"password\": \"1234aA5678\"}".getBytes());

        String expression = "$.[?(@.messages == ['%s'])]";

        //이름 null 값
        mvc.perform(multipart("/auth/signup")
                        .file(failFile1).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath(expression, USERNAME_EMPTY_MESSAGE).exists());
        Assertions.assertThat(userRepository.findByNickname("짱구")).isEmpty();

        //이름 정규식 불 일치
        mvc.perform(multipart("/auth/signup")
                        .file(failFile2).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath(expression, USERNAME_ERROR_MESSAGE).exists());
        Assertions.assertThat(userRepository.findByNickname("짱구")).isEmpty();
    }

    @DisplayName("회원가입 시 비밀번호 값이 null 혹은 정규식 불 일치 - 상태코드 400, 에러 메세지를 반환, db에 저장 실패")
    @Test
    void 회원가입실패테스트6() throws Exception {

        //비밀번호 null 값
        MockMultipartFile failFile1 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"짱구\", \"username\": \"신땡땡\", \"email\": \"orange@gmail.com\"}".getBytes());

        //정규식 불일치
        MockMultipartFile failFile2 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"짱구\", \"username\": \"신땡땡\", \"email\": \"orange@gmail.com\", \"password\": \"123456789\"}".getBytes());

        String expression = "$.[?(@.messages == ['%s'])]";

        //비밀번호 null 값
        mvc.perform(multipart("/auth/signup")
                        .file(failFile1).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath(expression, SECRET_EMPTY_MESSAGE).exists());
        Assertions.assertThat(userRepository.findByNickname("짱구")).isEmpty();

        //비밀번호 정규식 불 일치
        mvc.perform(multipart("/auth/signup")
                        .file(failFile2).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath(expression, SECRET_ERROR_MESSAGE).exists());
        Assertions.assertThat(userRepository.findByNickname("짱구")).isEmpty();
    }

    @DisplayName("회원가입 시 (여러) 값이 null 혹은 정규식 불 일치 - 상태코드 400, 에러 메세지를 반환, db에 저장 실패")
    @Test
    void 회원가입실패테스트7() throws Exception {

        //이메일, 닉네임, 이름, 비밀번호 중 null 값
        MockMultipartFile failFile3 = new MockMultipartFile("data", "", "application/json", "{}".getBytes());

        //정규식 불일치
        MockMultipartFile failFile4 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"오렌지1234567890\", \"username\": \"신땡땡1\", \"email\": \"orange@gmailcom\", \"password\": \"1234567890\"}".getBytes());

        String expression = "$.[?(@.messages == ['%s', '%s', '%s', '%s'])]";

        //이메일, 닉네임, 이름, 비밀번호 null 값
        mvc.perform(multipart("/auth/signup")
                        .file(failFile3).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath(
                        expression,
                        NICKNAME_EMPTY_MESSAGE,
                        SECRET_EMPTY_MESSAGE,
                        USERNAME_EMPTY_MESSAGE,
                        EMAIL_EMPTY_MESSAGE
                ).exists());
        Assertions.assertThat(userRepository.count()).isEqualTo(0);

        //이메일, 닉네임, 이름, 비밀번호 정규식 불 일치
        mvc.perform(multipart("/auth/signup")
                        .file(failFile4).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath(
                        expression,
                        USERNAME_ERROR_MESSAGE,
                        SECRET_ERROR_MESSAGE,
                        EMAIL_ERROR_MESSAGE,
                        NICKNAME_ERROR_MESSAGE
                ).exists());
        Assertions.assertThat(userRepository.findByNickname("오렌지1234567890")).isEmpty();
    }
}