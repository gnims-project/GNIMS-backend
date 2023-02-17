package com.gnims.project.domain.user.service;

import com.gnims.project.domain.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;

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

    @Autowired
    PlatformTransactionManager transactionManager;

    TransactionStatus status = null;

    @DisplayName("회원가입 성공 - 상태코드 201, 성공 메세지를 반환, db에 저장")
    @Test
    void 회원가입성공테스트() throws Exception {

        MockMultipartFile signupFile1 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"딸기\",\"username\": \"이땡땡\", \"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}".getBytes());
        MockMultipartFile signupFile2 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"햄버거\",\"username\": \"정땡땡\", \"email\": \"ddalgi2@gmail.com\", \"password\": \"123456aA9\"}".getBytes());

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

        //테스트 할 때마다 S3에 들어가기 때문에 추가했습니다.
        Assertions.assertThat(imageFile.getOriginalFilename()).isEqualTo("르탄이.png");

        //이미지 있을 때
        mvc.perform(multipart("/auth/signup")
                .file(signupFile1)/*.file(imageFile)*/.characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath(expression, "회원가입 완료").exists());
        Assertions.assertThat(userRepository.findByNickname("딸기").get()).isNotNull();

        //이미지 없을 때
        mvc.perform(multipart("/auth/signup")
                        .file(signupFile2).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath(expression, "회원가입 완료").exists());

        Assertions.assertThat(userRepository.findByNickname("햄버거").get()).isNotNull();
    }

    @DisplayName("회원가입 시 이메일 혹은 닉네임 중복 - 상태코드 400, 에러 메세지를 반환, db에 저장 실패")
    @Test
    void 회원가입실패테스트1() throws Exception {

        //중복 체크를 위해 db에 넣을 데이터
        MockMultipartFile successFile = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"딸기\",\"username\": \"이땡땡\", \"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}".getBytes());

        //이메일 중복
        MockMultipartFile failFile1 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"김밥\",\"username\": \"박땡땡\", \"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}".getBytes());

        //닉네임 중복
        MockMultipartFile failFile2 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"딸기\",\"username\": \"김땡땡\", \"email\": \"ddalgi2@gmail.com\", \"password\": \"123456aA9\"}".getBytes());

        String expression1 = "$.[?(@.message == '%s')]";

        mvc.perform(multipart("/auth/signup")
                        .file(successFile).characterEncoding("utf-8"));

        //이메일 중복
        mvc.perform(multipart("/auth/signup")
                        .file(failFile1).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath(expression1, "이미 등록된 이메일 입니다").exists());

        Assertions.assertThat(userRepository.findByNickname("김밥")).isEmpty();

        //닉네임 중복
        mvc.perform(multipart("/auth/signup")
                        .file(failFile2).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath(expression1, "중복된 닉네임 입니다").exists());

        Assertions.assertThat(userRepository.findByEmail("Gnims.Auth." + "ddalgi2@gmail.com")).isEmpty();
    }

    @DisplayName("회원가입 시 값이 null 혹은 정규식 불 일치 - 상태코드 400, 에러 메세지를 반환, db에 저장 실패")
    @Test
    void 회원가입실패테스트2() throws Exception {

        //이메일, 닉네임, 이름, 비밀번호 중 null 값
        MockMultipartFile failFile3 = new MockMultipartFile("data", "", "application/json", "{}".getBytes());

        //정규식 불일치
        MockMultipartFile failFile4 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"오렌지1234567890\",\"username\": \"신땡땡1\", \"email\": \"orange@gmailcom\", \"password\": \"1234567890\"}".getBytes());

        String expression2 = "$.[?(@.messages == ['%s', '%s', '%s', '%s'])]";

        //이메일, 닉네임, 이름, 비밀번호 null 값
        mvc.perform(multipart("/auth/signup")
                        .file(failFile3).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath(
                        expression2,
                        "닉네임은 필수 입력 값입니다.",
                        "비밀번호는 필수 입력 값입니다.",
                        "이름은 필수 입력 값입니다.",
                        "이메일은 필수 입력 값입니다."
                ).exists());
        Assertions.assertThat(userRepository.count()).isEqualTo(0);

        //이메일, 닉네임, 이름, 비밀번호 정규식 불 일치
        mvc.perform(multipart("/auth/signup")
                        .file(failFile4).characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath(
                        expression2,
                        "12자 이내의 한글, 영어 이름만 가능합니다.",
                        "비밀번호는 영문/숫자를 포함하여 8~16자로 입력해야합니다.",
                        "올바른 형식의 이메일 주소여야 합니다",
                        "특수문자를 제외한 2 ~ 8 자리의 닉네임만 가능합니다."
                ).exists());
        Assertions.assertThat(userRepository.findByNickname("오렌지1234567890")).isEmpty();
    }
}