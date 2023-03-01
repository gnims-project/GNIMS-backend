package com.gnims.project.social.sevice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gnims.project.domain.user.dto.LoginResponseDto;
import com.gnims.project.domain.user.entity.SocialCode;
import com.gnims.project.domain.user.entity.User;
import com.gnims.project.domain.user.repository.UserRepository;
import com.gnims.project.security.jwt.JwtUtil;
import com.gnims.project.social.dto.SocialEmailDto;
import com.gnims.project.social.dto.SocialProfileDto;
import com.gnims.project.social.dto.SocialResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoService {
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

    public SocialResult kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {

        // 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getToken(code);

        // 2. 토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
        SocialProfileDto kakaoUserInfo = getKakaoUserInfo(accessToken);

        // DB 에 중복된 Kakao email 이 있는지 확인
        String kakaoEmail = SocialCode.KAKAO.getValue() + kakaoUserInfo.getEmail();
        Optional<User> optionalKakaoUser = userRepository.findByEmail(kakaoEmail);

        // DB 에 없을 때 "non-member:"
        if (optionalKakaoUser.isEmpty()) {
            return new SocialResult(HttpStatus.OK.value(), "non-member", new SocialEmailDto(kakaoUserInfo.getEmail()));
        }
        User kakaoUser = optionalKakaoUser.get();

        // 4. JWT 토큰 담기
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, jwtUtil.createToken(kakaoUser.getNickname()));

        return new SocialResult(HttpStatus.OK.value(), "member", new LoginResponseDto(kakaoUser));
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
}