package com.gnims.project.social.sevice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gnims.project.domain.user.entity.User;
import com.gnims.project.domain.user.repository.UserRepository;
import com.gnims.project.security.jwt.JwtUtil;
import com.gnims.project.social.dto.SocialLoginDto;
import com.gnims.project.social.dto.SocialProfileDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${kakao.tokenUri}")
    private String kakaoTokenUri;

    @Value("${kakao.clientId}")
    private String kakaoClientId;

    @Value("${kakao.redirectUri}")
    private String kakaoCallbackUri;

    @Value("${kakao.userInfoUri}")
    private String kakaoProfileUri;

    public SocialLoginDto kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {

        // 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getToken(code);

        // 2. 토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
        SocialProfileDto kakaoUserInfo = getKakaoUserInfo(accessToken);

        // DB 에 중복된 Kakao email 이 있는지 확인
        String kakaoEmail = "Gnims.Kakao." + kakaoUserInfo.getEmail();
        User kakaoUser = userRepository.findByEmail(kakaoEmail)
                .orElse(null);
        if (kakaoUser == null) {
            throw new IllegalArgumentException("non-member");
        }

        // 4. JWT 토큰 담기
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, jwtUtil.createToken(kakaoUser.getNickname()));

        return new SocialLoginDto("member", kakaoUser.getNickname(), kakaoUserInfo.getEmail(), kakaoUser.getProfileImage());
    }

    // 1. "인가 코드"로 "액세스 토큰" 요청
    private String getToken(String code) throws JsonProcessingException {

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoClientId);
        body.add("redirect_uri", kakaoCallbackUri);
        body.add("code", code);

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.postForEntity(
                kakaoTokenUri,
                kakaoTokenRequest,
                String.class
        );

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("access_token").asText();
    }

    // 2. 토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
    private SocialProfileDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.postForEntity(
                kakaoProfileUri,
                kakaoUserInfoRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        String email = jsonNode.get("kakao_account")
                .get("email").asText();

        log.info("카카오 사용자 정보: " + email);
        return new SocialProfileDto(email);
    }

//    // 3. 필요시에 회원가입
//    private User registerKakaoUserIfNeeded(SocialProfileDto kakaoUserInfo) {
//
//        // DB 에 중복된 Kakao email 가 있는지 확인
//        String kakaoEmail = "Gnims.Kakao." + kakaoUserInfo.getEmail();
//        User kakaoUser = userRepository.findByEmail(kakaoEmail)
//                .orElse(null);
//        if (kakaoUser == null) {
//            kakaoUser = new User();
//        }
//        return kakaoUser;
//    }


    /*
    *
    * 이메일 유저와
    * 소셜 로그인 유저
    * 통합 x
    * */

//    //DB에서 사용자의 카카오 정보를 추가, 없을 시 DB에 저장 후 가져옴
//    User getKakaoUser(SocialProfileDto kakaoUserInfo, String kakaoEmail) {
//
//        //카카오 이메일과 동일한 이메일 유저가 있는경우
//        if (userRepository.findByEmail(kakaoEmail).isPresent()) {
//            return userRepository
//                    .findByEmail(kakaoEmail).get()
//                    // 기존 회원정보에 카카오 Id 추가
//                    .socialIdUpdate(SocialCode.KAKAO, kakaoId);
//        }
//
//        // 신규 회원가입
//        // password: random UUID
//        String password = UUID.randomUUID().toString();
//        String encodedPassword = passwordEncoder.encode(password);
//
//        // email: kakao email
//        String email = kakaoUserInfo.getEmail();
//
//        User kakaoUser = new User(kakaoUserInfo.getNickname(), SocialCode.KAKAO, kakaoId, email, encodedPassword);
//
//        userRepository.save(kakaoUser);
//        return kakaoUser;
//    }
}