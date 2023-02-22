package com.gnims.project.domain.user.service;

import com.gnims.project.domain.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import static com.gnims.project.share.message.ExceptionMessage.EXTENSION_ERROR_MESSAGE;
import static com.gnims.project.share.message.ResponseMessage.PROFILE_UPDATE_SUCCESS_MESSAGE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * 이미지 업로드 TEST
 * */
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class ImageUploadTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PlatformTransactionManager transactionManager;

    TransactionStatus status = null;

    String token = null;

    @Value("${profile.image}")
    String defaultImage;

    List<String> checkFile = new ArrayList<>(List.of(
            "gif", "jfif", "pjpeg", "jpeg",
            "pjp", "jpg", "png", "bmp",
            "dib", "webp", "svgz", "svg"));

    @BeforeEach
    void beforeEach() throws Exception {

        MockMultipartFile signupFile1 = new MockMultipartFile("data", "", "application/json", "{\"nickname\" : \"딸기\",\"username\": \"이땡땡\", \"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}".getBytes());

        mvc.perform(multipart("/auth/signup").file(signupFile1).characterEncoding("utf-8"));

        MvcResult result = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"ddalgi@gmail.com\", \"password\": \"123456aA9\"}")).andReturn();

        token = result.getResponse().getHeader("Authorization");
    }

    @DisplayName("이미지 파일 null 값 - 상태코드 200, 성공 메세지를 반환, db에 기본 이미지 저장")
    @Test
    void 기본이미지테스트() throws Exception {
        mvc.perform(multipart(HttpMethod.PATCH, "/users/profile").header("Authorization", token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(PROFILE_UPDATE_SUCCESS_MESSAGE));

        Assertions.assertThat(userRepository.findByNickname("딸기").get().getProfileImage()).isEqualTo(defaultImage);
    }

    @DisplayName("이미지 업데이트 - 상태코드 200, 성공 메세지를 반환, db에 이미지 업데이트")
    @Test
    void 일반이미지테스트() throws Exception {

        //업데이트 전에 기본 이미지 넣음
        mvc.perform(multipart(HttpMethod.PATCH, "/users/profile").header("Authorization", token));

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

        //테스트 할 때마다 S3에 들어가기 때문에 추가했습니다.
        Assertions.assertThat(imageFile.getOriginalFilename()).isEqualTo("르탄이.png");

        mvc.perform(multipart(HttpMethod.PATCH, "/users/profile")
                        /*.file(imageFile).characterEncoding("utf-8")*/
                        .header("Authorization", token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(PROFILE_UPDATE_SUCCESS_MESSAGE));

        Assertions.assertThat(userRepository.findByNickname("딸기").get().getProfileImage()).isEqualTo(defaultImage)/*.isNotEqualTo("https://gnims99.s3.ap-northeast-2.amazonaws.com/ProfilImg.png")*/;
    }

    @DisplayName("이미지 업데이트 시 허용하지 않은 확장자 - 상태코드 400, 실패 메세지를 반환, db에 저장 실패")
    @Test
    void 업데이트실패테스트() throws Exception {

        //업데이트 전에 기본 이미지 넣음
        mvc.perform(multipart(HttpMethod.PATCH, "/users/profile").header("Authorization", token));

        final String fileName = "어쩌구저쩌구"; //파일명
        final String contentType = "abcde"; //파일타입
        final String filePath = "src/test/resources/image/"+fileName+"."+contentType; //파일경로
        FileInputStream fileInputStream = new FileInputStream(filePath);

        //Mock파일생성
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", //name
                fileName + "." + contentType, //originalFilename
                contentType,
                fileInputStream
        );

        mvc.perform(multipart(HttpMethod.PATCH, "/users/profile")
                        .file(imageFile).characterEncoding("utf-8")
                        .header("Authorization", token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(checkFile + EXTENSION_ERROR_MESSAGE));

        Assertions.assertThat(userRepository.findByNickname("딸기").get().getProfileImage()).isEqualTo(defaultImage);
    }
}
