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

import static com.gnims.project.share.message.ResponseMessage.MEMBER_MESSAGE;
import static com.gnims.project.share.message.ResponseMessage.NON_MEMBER_MESSAGE;

@Service
@RequiredArgsConstructor
public class KakaoService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${kakao.clientId}")
    private String kakaoClientId;       //카카오 서버에 접근하기 위한 id

    @Value("${kakao.redirectUri}")
    private String kakaoCallbackUri;    //카카오 액세스 토큰을 받기 위해 필요한 리다이렉트 uri

    @Value("${kakao.tokenUri}")
    private String kakaoTokenUri;       //카카오 사용자 정보를 받기 위해 필요한 액세스 토큰을 받는 uri

    @Value("${kakao.userInfoUri}")
    private String kakaoProfileUri;     //카카오 사용자 정보를 받는 uri

    public SocialResult kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {
        // 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getToken(code);

        // 2. 토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
        SocialProfileDto kakaoUserInfo = getKakaoUserInfo(accessToken);

        // DB 에 해당 카카오 이메일의 유저가 있는지 확인
        String kakaoEmail = SocialCode.KAKAO.getValue() + kakaoUserInfo.getEmail();
        Optional<User> optionalKakaoUser = userRepository.findByEmail(kakaoEmail);

        // DB 에 없을 경우 non-member 리턴
        if (optionalKakaoUser.isEmpty()) {
            return new SocialResult(HttpStatus.OK.value(),
                    NON_MEMBER_MESSAGE,
                    new SocialEmailDto(kakaoUserInfo.getEmail()));
        }

        //있을 경우 해당 유저의 토큰 생성 및 member 리턴
        User kakaoUser = optionalKakaoUser.get();

        // 3. JWT 토큰 담기
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, jwtUtil.createToken(kakaoUser.getNickname()));

        return new SocialResult(HttpStatus.OK.value(),
                MEMBER_MESSAGE,
                new LoginResponseDto(kakaoUser));
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
        // Header 에 카카오 서버에 넘길 액세스 토큰 담기
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

        //카카오 유저 이메일
        String email = jsonNode.get("kakao_account").get("email").asText();

        return new SocialProfileDto(email);
    }
}