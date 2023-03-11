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
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NaverService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${naver.userInfoUri}")
    private String naverProfileUri;     //네이버 사용자 정보를 받는 uri

    public SocialResult naverLogin(String token, HttpServletResponse response) throws JsonProcessingException {
        // 토큰으로 네이버 API 호출 : "액세스 토큰"으로 "네이버 사용자 정보" 가져오기
        SocialProfileDto naverUserInfo = getNaverUserInfo(token);

        // DB 에 해당 네이버 이메일의 유저가 있는지 확인
        String naverEmail = SocialCode.NAVER.getValue() + naverUserInfo.getEmail();
        Optional<User> optionalNaverUser = userRepository.findByEmail(naverEmail);

        // DB 에 없을 경우 non-member 리턴
        if (optionalNaverUser.isEmpty()) {
            return new SocialResult(HttpStatus.OK.value(),
                    "non-member",
                    new SocialEmailDto(naverUserInfo.getEmail()));
        }

        //있을 경우 해당 유저의 토큰 생성 및 member 리턴
        User naverUser = optionalNaverUser.get();

        // JWT 토큰 담기
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, jwtUtil.createToken(naverUser.getNickname()));

        return new SocialResult(HttpStatus.OK.value(),
                "member",
                new LoginResponseDto(naverUser));
    }

    // 토큰으로 네이버 API 호출 : "액세스 토큰"으로 "네이버 사용자 정보" 가져오기
    private SocialProfileDto getNaverUserInfo(String accessToken) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        // Header 에 네이버 서버에 넘길 access 토큰 담기
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> naverUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
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

        return new SocialProfileDto(email);
    }
}
