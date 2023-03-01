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
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NaverService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${naver.userInfoUri}")
    private String naverProfileUri;

    public SocialResult naverLogin(String token, HttpServletResponse response) throws JsonProcessingException {

        // 1. 토큰으로 네이버 API 호출 : "액세스 토큰"으로 "네이버 사용자 정보" 가져오기
        SocialProfileDto naverUserInfo = getNaverUserInfo(token);

        // DB 에 중복된 Naver email 이 있는지 확인
        String naverEmail = SocialCode.NAVER.getValue() + naverUserInfo.getEmail();
        Optional<User> optionalNaverUser = userRepository.findByEmail(naverEmail);

        if (optionalNaverUser.isEmpty()) {
            return new SocialResult(HttpStatus.OK.value(), "non-member", new SocialEmailDto(naverUserInfo.getEmail()));
        }
        User naverUser = optionalNaverUser.get();

        // 4. JWT 토큰 담기
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, jwtUtil.createToken(naverUser.getNickname()));

        return new SocialResult(HttpStatus.OK.value(), "member", new LoginResponseDto(naverUser));
    }

    // 2. 토큰으로 네이버 API 호출 : "액세스 토큰"으로 "네이버 사용자 정보" 가져오기
    private SocialProfileDto getNaverUserInfo(String accessToken) throws JsonProcessingException {

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        // Header 에 naver 에 넘길 access 토큰 담기
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> naverUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();

        //naver 서버에 요청해서 유저 정보를 받아옴
        ResponseEntity<String> response = rt.postForEntity(
                naverProfileUri,
                naverUserInfoRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        //네이버 유저 이메일
        String email = jsonNode.get("response").get("email").asText();
        log.info("네이버 사용자 정보: " + email);

        return new SocialProfileDto(email);
    }
}
